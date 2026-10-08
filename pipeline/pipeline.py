#!/usr/bin/env python3
"""Buffered pipeline: source video -> HLS ladder + poster + metadata + catalog.

For every video in videos.json:
  1. Downloads the source to .cache/sources/ (and extracts it if it's a .zip). Reused if present.
  2. Inspects the source with ffprobe (resolution, fps, duration, whether it has audio).
  3. Encodes the ladder qualities that do NOT exceed the source resolution
     (no invented qualities: a 480p source gets a ladder up to 480p).
  4. Writes master.m3u8 with BANDWIDTH/AVERAGE-BANDWIDTH measured from the real segments.
  5. Generates poster.jpg (thumbnail) and backdrop.jpg (large image for the detail screen).
  6. Writes the video's metadata.json and, at the end, media/catalog.json with the whole catalog.

Usage:
  python3 pipeline/pipeline.py                      # every video
  python3 pipeline/pipeline.py big-buck-bunny       # only some (by id)
  python3 pipeline/pipeline.py --max-seconds 60     # only the first 60 s (quick tests)
  python3 pipeline/pipeline.py --force              # redo even if the output exists

Uses only the Python standard library + ffmpeg/ffprobe.
"""
import argparse
import json
import math
import shutil
import subprocess
import sys
import time
import urllib.request
import zipfile
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VIDEOS_JSON = ROOT / "pipeline" / "videos.json"
CACHE = ROOT / ".cache" / "sources"
MEDIA = ROOT / "media"

# ABR ladder as "boxes": each quality is the largest size the video fits in while keeping its
# aspect ratio. That way a 1920×818 widescreen film is "1080p" (1920×818) and doesn't lose its rung,
# as it would when comparing height alone. Steps of ~1.6-1.8x between qualities (lesson from labs
# 04/05: with 3x jumps ABR skips qualities).
LADDER = [
    # name, box width, box height, video bitrate, H.264 level, CODECS string
    ("1080p", 1920, 1080, "5000k", "4.0", "avc1.640028"),
    ("720p",  1280,  720, "2800k", "3.1", "avc1.64001f"),
    ("540p",   960,  540, "1600k", "3.1", "avc1.64001f"),
    ("480p",   854,  480, "1200k", "3.0", "avc1.64001e"),
    ("360p",   640,  360,  "800k", "3.0", "avc1.64001e"),
]
SEGMENT_S = 4           # duration of each HLS segment
GOP_S = 2               # one keyframe every 2 s, aligned across all qualities
AUDIO = ["-c:a", "aac", "-b:a", "128k", "-ac", "2", "-ar", "48000"]
AUDIO_CODEC = "mp4a.40.2"


def log(msg):
    print(f"[{time.strftime('%H:%M:%S')}] {msg}", flush=True)


def run(cmd):
    result = subprocess.run(cmd, stdin=subprocess.DEVNULL, capture_output=True, text=True)
    if result.returncode != 0:
        sys.stderr.write(result.stderr[-3000:])
        raise RuntimeError(f"Failed: {' '.join(cmd[:6])} ...")
    return result.stdout


# --- 1. Download ------------------------------------------------------------------------------

def download(video):
    CACHE.mkdir(parents=True, exist_ok=True)
    url = video["source_url"]
    name = urllib.request.unquote(url.rsplit("/", 1)[-1])
    target = CACHE / name
    if not target.exists():
        log(f"  downloading {name} ...")
        partial = target.with_suffix(target.suffix + ".part")
        # Some servers (download.blender.org) reject urllib's default User-Agent with a 403.
        request = urllib.request.Request(url, headers={"User-Agent": "buffered-pipeline/1.0 (+ffmpeg)"})
        with urllib.request.urlopen(request) as resp, open(partial, "wb") as f:
            shutil.copyfileobj(resp, f, length=1024 * 1024)
        partial.rename(target)
    if "zip_member" not in video:
        return target
    extracted = CACHE / video["zip_member"]
    if not extracted.exists():
        log(f"  extracting {video['zip_member']} ...")
        with zipfile.ZipFile(target) as z:
            member = next(n for n in z.namelist() if n.endswith(video["zip_member"]))
            with z.open(member) as src, open(extracted, "wb") as dst:
                shutil.copyfileobj(src, dst, length=1024 * 1024)
    return extracted


# --- 2. Inspection ----------------------------------------------------------------------------

def source_filters(video):
    """Per-video cleanup applied before any scaling: deinterlace and crop baked-in black borders."""
    filters = []
    if video.get("deinterlace"):
        filters.append("bwdif=mode=send_frame")
    if video.get("crop"):
        filters.append(f"crop={video['crop']}")
    return filters


def inspect(source, video):
    data = json.loads(run([
        "ffprobe", "-v", "error", "-print_format", "json",
        "-show_streams", "-show_format", str(source),
    ]))
    stream = next(s for s in data["streams"] if s["codec_type"] == "video")
    num, den = (int(x) for x in stream.get("avg_frame_rate", "0/1").split("/"))
    fps = num / den if den else 0
    if not fps:
        num, den = (int(x) for x in stream["r_frame_rate"].split("/"))
        fps = num / den
    # Display width taking the sample aspect ratio (SAR) into account: some SD sources don't have
    # square pixels.
    sar = stream.get("sample_aspect_ratio", "1:1")
    sn, sd = (int(x) for x in sar.split(":")) if sar not in ("0:1", "N/A") else (1, 1)
    width, height = round(stream["width"] * sn / sd), stream["height"]
    if video.get("crop"):
        width, height = (int(x) for x in video["crop"].split(":")[:2])
    return {
        "width": width,
        "height": height,
        "fps": fps,
        "duration": float(data["format"]["duration"]),
        "has_audio": not video.get("mute") and any(s["codec_type"] == "audio" for s in data["streams"]),
    }


# --- 3. Ladder encoding -----------------------------------------------------------------------

def pick_rungs(info):
    """Qualities to generate: the boxes the source fills in width or height. Never upscales."""
    sw, sh = info["width"], info["height"]
    hd = sw >= 960 or sh >= 540
    rungs = [r for r in LADDER if sw >= r[1] or sh >= r[2]]
    if hd:
        # 480p only exists for SD sources: with HD sources it would sit right next to 540p.
        rungs = [r for r in rungs if r[0] != "480p"]
    elif not rungs or sh > rungs[0][2]:
        # Odd SD source (e.g. 608×448): add its native resolution as the top quality, so it isn't
        # thrown away by scaling it down to 360p.
        rungs.insert(0, (f"{sh}p", sw, sh, "1200k", "3.0", "avc1.64001e"))
    return rungs or [LADDER[-1]]


def encode(video, source, info, output, max_seconds):
    renditions = []
    cleanup = "".join(f + "," for f in source_filters(video))
    for name, box_w, box_h, bitrate, level, codecs in pick_rungs(info):
        folder = output / name
        folder.mkdir(parents=True, exist_ok=True)
        log(f"  encoding {name} ({bitrate}) ...")
        bufsize = f"{int(bitrate[:-1]) * 2}k"
        cmd = ["ffmpeg", "-hide_banner", "-y", "-i", str(source)]
        if max_seconds:
            cmd += ["-t", str(max_seconds)]
        cmd += [
            "-map", "0:v:0",
            *(["-map", "0:a:0"] if info["has_audio"] else []),
            # Fits the box without distortion; even dimensions (H.264 4:2:0); square pixels.
            "-vf", (f"{cleanup}scale=w={box_w}:h={box_h}:force_original_aspect_ratio=decrease:flags=lanczos,"
                    "scale=trunc(iw/2)*2:trunc(ih/2)*2,setsar=1,format=yuv420p"),
            "-c:v", "libx264", "-preset", "veryfast", "-profile:v", "high", "-level", level,
            "-b:v", bitrate, "-maxrate", bitrate, "-bufsize", bufsize,
            # A keyframe every GOP_S seconds by time, not by frame count: that keeps it aligned across
            # qualities whatever the source fps. sc_threshold 0 prevents extra keyframes.
            "-force_key_frames", f"expr:gte(t,n_forced*{GOP_S})", "-sc_threshold", "0",
            *(AUDIO if info["has_audio"] else ["-an"]),
            "-f", "hls", "-hls_time", str(SEGMENT_S), "-hls_playlist_type", "vod",
            "-hls_segment_filename", str(folder / "seg_%04d.ts"),
            str(folder / "index.m3u8"),
        ]
        run(cmd)
        peak, average, real_width, real_height = measure(folder)
        renditions.append({
            "name": name, "width": real_width, "height": real_height,
            "peakBandwidth": peak, "averageBandwidth": average,
            "codecs": codecs + (f",{AUDIO_CODEC}" if info["has_audio"] else ""),
            "playlist": f"{name}/index.m3u8",
        })
    return renditions


def measure(folder):
    """Real peak and average bitrate (bits/s) from the segments and their durations."""
    lines = (folder / "index.m3u8").read_text().splitlines()
    peak = total_bits = total_s = 0
    dur = None
    for line in lines:
        if line.startswith("#EXTINF:"):
            dur = float(line[8:].split(",")[0])
        elif line.endswith(".ts") and dur:
            bits = (folder / line).stat().st_size * 8
            peak = max(peak, bits / dur)
            total_bits += bits
            total_s += dur
    first_segment = next(line for line in lines if line.endswith(".ts"))
    stream = json.loads(run([
        "ffprobe", "-v", "error", "-select_streams", "v:0", "-show_entries", "stream=width,height",
        "-print_format", "json", str(folder / first_segment),
    ]))["streams"][0]
    # Rounded up to 10 kb/s: BANDWIDTH must be >= the real peak.
    return math.ceil(peak / 10_000) * 10_000, round(total_bits / total_s), stream["width"], stream["height"]


def write_master(output, renditions):
    lines = ["#EXTM3U", "#EXT-X-VERSION:3", "#EXT-X-INDEPENDENT-SEGMENTS"]
    for r in renditions:  # highest to lowest, as in LADDER
        lines.append(
            f"#EXT-X-STREAM-INF:BANDWIDTH={r['peakBandwidth']},AVERAGE-BANDWIDTH={r['averageBandwidth']},"
            f"RESOLUTION={r['width']}x{r['height']},CODECS=\"{r['codecs']}\""
        )
        lines.append(r["playlist"])
    (output / "master.m3u8").write_text("\n".join(lines) + "\n")


# --- 4. Images --------------------------------------------------------------------------------

def images(video, source, info, output, max_seconds):
    duration = min(info["duration"], max_seconds) if max_seconds else info["duration"]
    # "poster_seconds" in videos.json picks the frame by hand; otherwise 20% in usually skips the
    # opening credits and black frames.
    at = video.get("poster_seconds")
    if at is None or at >= duration:
        at = duration * 0.2
    at = f"{at:.1f}"
    for name, width in (("poster.jpg", 640), ("backdrop.jpg", 1280)):
        run([
            "ffmpeg", "-hide_banner", "-y", "-ss", at, "-i", str(source), "-frames:v", "1",
            "-vf", ",".join([*source_filters(video), f"scale={width}:-2:flags=lanczos", "setsar=1"]),
            "-q:v", "3", str(output / name),
        ])


# --- Orchestration ----------------------------------------------------------------------------

def process(video, max_seconds, force):
    output = MEDIA / video["id"]
    meta_path = output / "metadata.json"
    if meta_path.exists() and not force:
        log(f"{video['id']}: already exists, reusing it (use --force to redo)")
        return json.loads(meta_path.read_text())
    log(f"{video['id']}: processing")
    source = download(video)
    info = inspect(source, video)
    log(f"  source: {info['width']}x{info['height']} @ {info['fps']:.2f} fps, "
        f"{info['duration'] / 60:.1f} min, audio={'yes' if info['has_audio'] else 'no'}")
    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True)
    renditions = encode(video, source, info, output, max_seconds)
    write_master(output, renditions)
    images(video, source, info, output, max_seconds)
    duration = min(info["duration"], max_seconds) if max_seconds else info["duration"]
    meta = {
        "id": video["id"],
        "title": video["title"],
        "year": video["year"],
        "description": video["description"],
        "durationSeconds": round(duration),
        "license": video["license"],
        "attribution": video["attribution"],
        "sourceUrl": video["source_url"],
        "master": "master.m3u8",
        "poster": "poster.jpg",
        "backdrop": "backdrop.jpg",
        "renditions": renditions,
        "trimmedForTesting": bool(max_seconds),
    }
    meta_path.write_text(json.dumps(meta, indent=2, ensure_ascii=False) + "\n")
    log(f"  done: {', '.join(r['name'] for r in renditions)}")
    return meta


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("ids", nargs="*", help="ids of the videos to process (default: all)")
    p.add_argument("--max-seconds", type=int, default=0, help="process only the first N seconds")
    p.add_argument("--force", action="store_true", help="redo even if the output already exists")
    args = p.parse_args()

    for tool in ("ffmpeg", "ffprobe"):
        if not shutil.which(tool):
            sys.exit(f"{tool} is missing from PATH")

    videos = json.loads(VIDEOS_JSON.read_text())["videos"]
    if args.ids:
        unknown = set(args.ids) - {v["id"] for v in videos}
        if unknown:
            sys.exit(f"unknown ids: {', '.join(sorted(unknown))}")
        videos = [v for v in videos if v["id"] in args.ids]

    for v in videos:
        process(v, args.max_seconds, args.force)

    # The catalog is always rebuilt from everything in media/, in the order of videos.json.
    order = [v["id"] for v in json.loads(VIDEOS_JSON.read_text())["videos"]]
    entries = [json.loads((MEDIA / i / "metadata.json").read_text())
               for i in order if (MEDIA / i / "metadata.json").exists()]
    catalog = {"generatedAt": datetime.now(timezone.utc).isoformat(timespec="seconds"), "videos": entries}
    (MEDIA / "catalog.json").write_text(json.dumps(catalog, indent=2, ensure_ascii=False) + "\n")
    log(f"catalog.json: {len(entries)} videos")


if __name__ == "__main__":
    main()
