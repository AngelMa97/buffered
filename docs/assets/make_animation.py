#!/usr/bin/env python3
"""Renders the "how Buffered works" animation (MP4 for LinkedIn + GIF for the README).

    python3 docs/assets/make_animation.py

Requires Pillow and ffmpeg. Fonts: Inter (OFL) and JetBrains Mono in ~/.cache/buffered-fonts/
(Inter-{Regular,Medium,SemiBold,Bold}.ttf, JetBrainsMono-Medium.ttf). The phone image comes from
media/big-buck-bunny/backdrop.jpg (run the pipeline first).

The network and the player are simulated with simple rules that stay faithful to what was measured
in the labs: ABR picks a quality when requesting each segment using ~70% of the estimated
bandwidth, and what is on screen lags behind what is being downloaded because of the buffer.
"""
import math
import shutil
import subprocess
import tempfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUTPUT_DIR = Path(__file__).resolve().parent
FONTS = Path.home() / ".cache" / "buffered-fonts"
VIDEO_FRAME = ROOT / "media" / "big-buck-bunny" / "backdrop.jpg"

W, H = 1600, 900
SS = 2                      # supersampling for smooth edges
FPS = 30
DURATION = 16.0
N = int(DURATION * FPS)

# Palette (dark theme)
BG = (10, 14, 20)
GRID = (20, 27, 37)
CARD = (18, 24, 33)
CARD_BORDER = (40, 50, 64)
TEXT = (232, 238, 245)
MUTED = (138, 152, 170)
ACCENT = (251, 146, 60)     # orange #FB923C: Buffered's identity
QUALITY_COLOR = {
    "1080p": (52, 211, 153),
    "720p": (96, 165, 250),
    "540p": (251, 191, 36),
    "360p": (248, 113, 113),
}
LADDER = [("1080p", 5.0), ("720p", 2.8), ("540p", 1.6), ("360p", 0.8)]


def font(name, size):
    return ImageFont.truetype(str(FONTS / name), size * SS)


F = {
    "title": font("Inter-Bold.ttf", 34),
    "sub": font("Inter-Regular.ttf", 19),
    "card": font("Inter-SemiBold.ttf", 19),
    "label": font("Inter-Medium.ttf", 13),
    "body": font("Inter-Regular.ttf", 16),
    "body_b": font("Inter-SemiBold.ttf", 16),
    "mono": font("JetBrainsMono-Medium.ttf", 14),
    "mono_s": font("JetBrainsMono-Medium.ttf", 12),
    "caption": font("Inter-SemiBold.ttf", 26),
    "caption_n": font("Inter-Bold.ttf", 26),
    "seg": font("Inter-Bold.ttf", 11),
}


# --- animation helpers ----------------------------------------------------------------------

def clamp(x, a=0.0, b=1.0):
    return max(a, min(b, x))


def ease(x):
    x = clamp(x)
    return x * x * (3 - 2 * x)


def ramp(t, start, end):
    """0→1 between start and end, eased."""
    return ease((t - start) / (end - start)) if end > start else float(t >= start)


def blend(c, alpha, background=BG):
    return tuple(round(background[i] + (c[i] - background[i]) * alpha) for i in range(3))


def s(*v):
    return [round(x * SS) for x in v]


# --- network and player simulation ----------------------------------------------------------

def bandwidth(t):
    """Available Mb/s: high, sharp drop, recovery."""
    if t < 8.6:
        return 9.0
    if t < 9.3:
        return 9.0 + (1.3 - 9.0) * ease((t - 8.6) / 0.7)
    if t < 12.0:
        return 1.3
    if t < 12.7:
        return 1.3 + (9.0 - 1.3) * ease((t - 12.0) / 0.7)
    return 9.0


STREAM_START = 6.0
SEGMENT_PERIOD = 0.55   # how often a segment is requested (compressed for the animation)


def simulate():
    """Per segment: request time, chosen quality and the time it reaches the screen."""
    segs = []
    estimate = 4.0                     # the estimator starts conservative
    t = STREAM_START
    while t < DURATION + 2:
        bw = bandwidth(t)
        estimate += (bw - estimate) * 0.55        # adapts within a few segments
        budget = estimate * 0.7              # the 70% rule
        quality = next((n for n, br in LADDER if br <= budget), "360p")
        # Going up is slower than going down (ExoPlayer needs more buffer to switch up):
        if segs:
            order = [n for n, _ in LADDER]
            previous = segs[-1]["q"]
            if order.index(quality) < order.index(previous) - 1:
                quality = order[order.index(previous) - 1]
        travel = clamp(0.25 + 1.6 / max(bw, 0.5), 0.35, 1.6)
        segs.append({"requested": t, "q": quality, "travel": travel, "arrives": t + travel})
        t += SEGMENT_PERIOD
    # What is on screen lags a few segments behind what was downloaded (the buffer)
    lag = 2
    for i, sg in enumerate(segs):
        sg["visible"] = segs[max(0, i - lag)]["q"] if i >= lag else "360p"
    return segs


SEGS = simulate()


def state(t):
    arrived = [sg for sg in SEGS if sg["arrives"] <= t]
    downloading = next((sg["q"] for sg in reversed(SEGS) if sg["requested"] <= t), "—")
    visible = arrived[-1]["visible"] if arrived else "—"
    # buffer: seconds "in the tank"; drains when the network drops
    if t < STREAM_START + 0.8:
        buf = 0.0
    else:
        buf = 22.0
        if t > 9.0:
            buf -= min(t - 9.0, 2.6) * 5.5
        if t > 12.3:
            buf += min(t - 12.3, 3.0) * 4.6
    return downloading, visible, clamp(buf, 0, 30), arrived


# --- drawing ----------------------------------------------------------------------------------

def card(d, x, y, w, h, title, alpha, icon=None):
    border = blend(CARD_BORDER, alpha)
    d.rounded_rectangle(s(x, y, x + w, y + h), radius=18 * SS, fill=blend(CARD, alpha), outline=border, width=2 * SS)
    d.text(s(x + 22, y + 18), title, font=F["label"], fill=blend(ACCENT, alpha))


def arrow(d, x1, y, x2, alpha, pulse=None):
    col = blend((70, 84, 104), alpha)
    d.line(s(x1, y, x2 - 10, y), fill=col, width=3 * SS)
    d.polygon(s(x2, y, x2 - 12, y - 7, x2 - 12, y + 7), fill=col)
    if pulse is not None and 0 <= pulse <= 1:
        px = x1 + (x2 - x1) * pulse
        r = 7
        d.ellipse(s(px - r, y - r, px + r, y + r), fill=blend(ACCENT, alpha))


def centered_text(d, cx, y, txt, f, col):
    w = d.textlength(txt, font=f) / SS
    d.text(s(cx - w / 2, y), txt, font=f, fill=col)


def video_frame(base, quality, width, height):
    """Phone image degraded to match the quality on screen (exaggerated so it shows)."""
    factor = {"1080p": 1.0, "720p": 0.42, "540p": 0.24, "360p": 0.11, "—": 0.11}[quality]
    img = base.resize((width, height), Image.LANCZOS)
    if factor < 1:
        small = img.resize((max(8, int(width * factor)), max(4, int(height * factor))), Image.BILINEAR)
        img = small.resize((width, height), Image.NEAREST).filter(ImageFilter.GaussianBlur(0.6 * SS))
    return img


def frame(t, base_video):
    img = Image.new("RGB", (W * SS, H * SS), BG)
    d = ImageDraw.Draw(img)

    # subtle grid
    for gx in range(0, W, 40):
        d.line(s(gx, 0, gx, H), fill=GRID, width=1)
    for gy in range(0, H, 40):
        d.line(s(0, gy, W, gy), fill=GRID, width=1)

    fade = 1 - ramp(t, DURATION - 0.6, DURATION)   # fade out at the end so it loops

    # Header
    a = ramp(t, 0.0, 0.6) * fade
    d.text(s(64, 46), "Buffered", font=F["title"], fill=blend(TEXT, a))
    d.text(s(66, 92), "How a film becomes an adaptive stream on your phone", font=F["sub"], fill=blend(MUTED, a))

    Y, HC = 170, 470   # row of cards

    # 1. Source
    a1 = ramp(t, 0.4, 1.0) * fade
    card(d, 64, Y, 230, HC, "SOURCE", a1)
    # film icon
    fx, fy = 104, Y + 70
    d.rounded_rectangle(s(fx, fy, fx + 150, fy + 100), radius=10 * SS, outline=blend(TEXT, a1), width=3 * SS)
    for k in range(5):
        d.rectangle(s(fx + 10 + k * 28, fy + 8, fx + 26 + k * 28, fy + 18), fill=blend(MUTED, a1))
        d.rectangle(s(fx + 10 + k * 28, fy + 82, fx + 26 + k * 28, fy + 92), fill=blend(MUTED, a1))
    d.polygon(s(fx + 62, fy + 34, fx + 62, fy + 66, fx + 92, fy + 50), fill=blend(ACCENT, a1))
    d.text(s(88, Y + 200), "big_buck_bunny.mp4", font=F["mono"], fill=blend(TEXT, a1))
    d.text(s(88, Y + 226), "1920×1080 · 30 fps", font=F["body"], fill=blend(MUTED, a1))
    d.text(s(88, Y + 290), "Creative Commons /", font=F["body"], fill=blend(MUTED, a1))
    d.text(s(88, Y + 314), "public domain films", font=F["body"], fill=blend(MUTED, a1))

    arrow(d, 300, Y + HC / 2, 350, ramp(t, 1.0, 1.4) * fade, pulse=(t - 1.6) / 0.6)

    # 2. Pipeline with the ladder
    a2 = ramp(t, 1.2, 1.8) * fade
    card(d, 356, Y, 300, HC, "PIPELINE · FFmpeg", a2)
    d.text(s(378, Y + 50), "Bitrate ladder", font=F["card"], fill=blend(TEXT, a2))
    for i, (q, br) in enumerate(LADDER):
        grow = ramp(t, 2.1 + i * 0.35, 2.7 + i * 0.35) * fade
        by = Y + 100 + i * 62
        d.text(s(378, by + 4), q, font=F["body_b"], fill=blend(TEXT, a2))
        length = 128 * br / 5.0 * grow
        d.rounded_rectangle(s(448, by + 2, 448 + max(length, 1), by + 26), radius=6 * SS,
                            fill=blend(QUALITY_COLOR[q], a2 * (0.25 + 0.75 * grow)))
        d.text(s(452 + max(length, 1) + 6, by + 5), f"{br:.1f} Mb/s", font=F["mono_s"], fill=blend(MUTED, a2 * grow))
    d.text(s(378, Y + 360), "Keyframes every 2 s,", font=F["body"], fill=blend(MUTED, a2))
    d.text(s(378, Y + 384), "aligned across qualities", font=F["body"], fill=blend(MUTED, a2))
    d.text(s(378, Y + 416), "→ 4 s HLS segments", font=F["body_b"], fill=blend(TEXT, a2))

    arrow(d, 662, Y + HC / 2, 712, ramp(t, 3.6, 4.0) * fade, pulse=(t - 4.0) / 0.6)

    # 3. Server
    a3 = ramp(t, 3.8, 4.4) * fade
    card(d, 718, Y, 260, HC, "SERVER · Ktor", a3)
    rows = [
        ("GET /api/videos", 4.6), ("GET /api/videos/{id}", 4.85),
        ("GET /media/…/master.m3u8", 5.1), ("GET /media/…/720p/index.m3u8", 5.35),
    ]
    for i, (txt, ta) in enumerate(rows):
        af = ramp(t, ta, ta + 0.3) * a3
        d.text(s(740, Y + 56 + i * 30), txt, font=F["mono_s"], fill=blend(TEXT if i < 2 else MUTED, af))
    # served segments (the latest requests)
    requested_segs = [sg for sg in SEGS if sg["requested"] <= t][-6:]
    for i, sg in enumerate(requested_segs):
        yy = Y + 200 + i * 28
        col = QUALITY_COLOR[sg["q"]]
        d.rounded_rectangle(s(740, yy, 752, yy + 12), radius=3 * SS, fill=blend(col, a3))
        idx = SEGS.index(sg)
        d.text(s(760, yy - 3), f"{sg['q']}/seg_{idx:04d}.ts", font=F["mono_s"], fill=blend(TEXT, a3))
    d.text(s(740, Y + 384), "Catalog API + HLS files", font=F["body"], fill=blend(MUTED, a3))
    d.text(s(740, Y + 408), "Demo mode: throttle", font=F["body"], fill=blend(MUTED, a3))
    d.text(s(740, Y + 430), "the network on demand", font=F["body"], fill=blend(MUTED, a3))

    # 4. Network: pipe with segments in flight + meter
    a4 = ramp(t, 5.2, 5.8) * fade
    cx1, cx2, cy = 990, 1222, Y + HC / 2
    d.line(s(cx1, cy, cx2, cy), fill=blend((50, 62, 78), a4), width=24 * SS)
    for sg in SEGS:
        if sg["requested"] <= t < sg["arrives"]:
            p = (t - sg["requested"]) / sg["travel"]
            px = cx1 + 14 + (cx2 - cx1 - 28) * p
            col = QUALITY_COLOR[sg["q"]]
            d.rounded_rectangle(s(px - 22, cy - 11, px + 22, cy + 11), radius=5 * SS, fill=blend(col, a4))
            centered_text(d, px, cy - 8, sg["q"], F["seg"], blend(BG, a4))
    bw = bandwidth(t)
    centered_text(d, (cx1 + cx2) / 2, cy - 64, "NETWORK", F["label"], blend(ACCENT, a4))
    col_bw = QUALITY_COLOR["1080p"] if bw > 5 else QUALITY_COLOR["360p"]
    centered_text(d, (cx1 + cx2) / 2, cy - 42, f"{bw:4.1f} Mb/s", F["body_b"], blend(col_bw, a4))
    # bandwidth chart
    gx1, gy1, gw, gh = cx1 + 6, cy + 42, cx2 - cx1 - 12, 70
    d.rounded_rectangle(s(gx1, gy1, gx1 + gw, gy1 + gh), radius=8 * SS, outline=blend(CARD_BORDER, a4), width=1 * SS)
    points = []
    for k in range(80):
        tt = STREAM_START + (DURATION - STREAM_START) * k / 79
        if tt > t:
            break
        points.append((gx1 + 6 + (gw - 12) * k / 79, gy1 + gh - 8 - (gh - 16) * bandwidth(tt) / 10))
    if len(points) > 1 and t > STREAM_START:
        d.line([c * SS for p in points for c in p], fill=blend(ACCENT, a4), width=3 * SS, joint="curve")

    # 5. Phone
    a5 = ramp(t, 5.4, 6.0) * fade
    px, py, pw, ph = 1250, 128, 290, 560
    d.rounded_rectangle(s(px, py, px + pw, py + ph), radius=40 * SS, fill=blend((6, 9, 13), a5),
                        outline=blend((70, 84, 104), a5), width=4 * SS)
    d.rounded_rectangle(s(px + pw / 2 - 40, py + 14, px + pw / 2 + 40, py + 24), radius=5 * SS, fill=blend((40, 48, 60), a5))
    downloading, visible, buf, _ = state(t)
    vx, vy, vw, vh = px + 14, py + 46, pw - 28, int((pw - 28) * 9 / 16)
    if a5 > 0.01:
        video = video_frame(base_video, visible if t > STREAM_START + 0.8 else "360p", vw * SS, vh * SS)
        if a5 < 1:
            video = Image.blend(Image.new("RGB", video.size, BG), video, a5)
        img.paste(video, (vx * SS, vy * SS))
        if visible in QUALITY_COLOR:
            label = visible
            ew = d.textlength(label, font=F["seg"]) / SS + 14
            d.rounded_rectangle(s(vx + vw - ew - 8, vy + 8, vx + vw - 8, vy + 26), radius=5 * SS,
                                fill=blend(QUALITY_COLOR[visible], a5))
            d.text(s(vx + vw - ew - 1, vy + 11), label, font=F["seg"], fill=BG)
    d.text(s(px + 18, vy + vh + 16), "Big Buck Bunny", font=F["card"], fill=blend(TEXT, a5))
    d.text(s(px + 18, vy + vh + 42), "2008 · CC BY · Blender Foundation", font=F["mono_s"], fill=blend(MUTED, a5))

    # Stats for nerds
    sx, sy = px + 14, vy + vh + 82
    d.rounded_rectangle(s(sx, sy, px + pw - 14, sy + 232), radius=12 * SS, fill=blend((14, 20, 28), a5),
                        outline=blend(CARD_BORDER, a5), width=1 * SS)
    d.text(s(sx + 14, sy + 12), "STATS FOR NERDS", font=F["label"], fill=blend(ACCENT, a5))
    stat_rows = [
        ("Downloading", downloading, QUALITY_COLOR.get(downloading, TEXT)),
        ("On screen", visible, QUALITY_COLOR.get(visible, TEXT)),
        ("Bandwidth", f"{bw:.1f} Mb/s" if t > STREAM_START else "—", TEXT),
    ]
    for i, (k, v, c) in enumerate(stat_rows):
        yy = sy + 44 + i * 34
        d.text(s(sx + 14, yy), k, font=F["body"], fill=blend(MUTED, a5))
        vw_t = d.textlength(v, font=F["body_b"]) / SS
        d.text(s(px + pw - 28 - vw_t, yy), v, font=F["body_b"], fill=blend(c, a5))
    yy = sy + 150
    d.text(s(sx + 14, yy), "Buffer", font=F["body"], fill=blend(MUTED, a5))
    btxt = f"{buf:4.1f} s"
    bw_t = d.textlength(btxt, font=F["body_b"]) / SS
    d.text(s(px + pw - 28 - bw_t, yy), btxt, font=F["body_b"], fill=blend(TEXT, a5))
    bx1, bx2, by = sx + 14, px + pw - 28, yy + 34
    d.rounded_rectangle(s(bx1, by, bx2, by + 12), radius=6 * SS, fill=blend((34, 42, 54), a5))
    filled = (bx2 - bx1) * buf / 30
    if filled > 2:
        # Buffer health in green/amber: with the orange accent, "full" and "low" would look the same.
        col_b = QUALITY_COLOR["1080p"] if buf > 8 else QUALITY_COLOR["540p"]
        d.rounded_rectangle(s(bx1, by, bx1 + filled, by + 12), radius=6 * SS, fill=blend(col_b, a5))

    # Bottom caption (steps)
    steps = [
        (0.4, 2.0, "1", "Start from an open-licensed film"),
        (2.0, 3.8, "2", "Encode it once into a bitrate ladder with FFmpeg"),
        (3.8, 6.0, "3", "Serve the catalog and HLS segments with Ktor"),
        (6.0, 8.8, "4", "The player picks a quality for every segment it downloads"),
        (8.8, 12.4, "5", "Network drops → next segments in 360p, the buffer keeps playback smooth"),
        (12.4, DURATION, "6", "Network recovers → quality climbs back, step by step"),
    ]
    for start, step_end, n, txt in steps:
        ap = (ramp(t, start, start + 0.35) - ramp(t, step_end - 0.25, step_end)) if step_end < DURATION else ramp(t, start, start + 0.35)
        ap = clamp(ap) * fade
        if ap <= 0.01:
            continue
        cy0 = 712
        d.ellipse(s(64, cy0, 104, cy0 + 40), fill=blend(ACCENT, ap))
        centered_text(d, 84, cy0 + 6, n, F["caption_n"], blend(BG, ap))
        d.text(s(122, cy0 + 4), txt, font=F["caption"], fill=blend(TEXT, ap))

    # footer
    af = ramp(t, 0.4, 1.0) * fade
    d.text(s(64, 836), "pipeline: FFmpeg  ·  server: Ktor  ·  app: Android + Media3 (ExoPlayer), MVI", font=F["mono_s"],
           fill=blend(MUTED, af))
    d.text(s(1340, 836), "github.com/AngelMa97/buffered", font=F["mono_s"], fill=blend(MUTED, af))

    return img.resize((W, H), Image.LANCZOS)


def main():
    if not VIDEO_FRAME.exists():
        raise SystemExit("media/big-buck-bunny/backdrop.jpg is missing: run the pipeline first")
    base = Image.open(VIDEO_FRAME).convert("RGB")
    tmp = Path(tempfile.mkdtemp(prefix="buffered-anim-"))
    try:
        for i in range(N):
            frame(i / FPS, base).save(tmp / f"f_{i:04d}.png")
        mp4 = OUTPUT_DIR / "buffered-architecture.mp4"
        gif = OUTPUT_DIR / "buffered-architecture.gif"
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-framerate", str(FPS),
                        "-i", str(tmp / "f_%04d.png"), "-c:v", "libx264", "-preset", "slow", "-crf", "18",
                        "-pix_fmt", "yuv420p", "-movflags", "+faststart", str(mp4)], check=True)
        ffmpeg_filter = ("fps=15,scale=960:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=128:stats_mode=diff[p];"
                  "[b][p]paletteuse=dither=bayer:bayer_scale=4:diff_mode=rectangle")
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(mp4),
                        "-filter_complex", ffmpeg_filter, "-loop", "0", str(gif)], check=True)
        print(f"{mp4.name}: {mp4.stat().st_size / 1e6:.1f} MB · {gif.name}: {gif.stat().st_size / 1e6:.1f} MB")
    finally:
        shutil.rmtree(tmp)


if __name__ == "__main__":
    main()
