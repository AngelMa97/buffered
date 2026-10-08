# pipeline

Turns every video in [`videos.json`](videos.json) into an HLS stream ready to serve:

```
media/
├── catalog.json                 ← full catalog (what the server reads)
└── <id>/
    ├── master.m3u8              ← master playlist with the ladder
    ├── 1080p/ 720p/ 540p/ 360p/ ← index.m3u8 + seg_0000.ts …
    ├── poster.jpg               ← 640 px wide (catalog grid)
    ├── backdrop.jpg             ← 1280 px wide (detail screen)
    └── metadata.json            ← same as the video's entry in catalog.json
```

`media/` and `.cache/` are in `.gitignore`: they weigh gigabytes and are regenerated.

## Usage

Requires `python3` (standard library only) and `ffmpeg`/`ffprobe`. From the repo root:

```bash
python3 pipeline/pipeline.py --max-seconds 60   # whole catalog, trimmed to 60 s (minutes)
python3 pipeline/pipeline.py                    # everything, full length (≈5 GB download, slow)
python3 pipeline/pipeline.py sintel --force     # redo a single video
```

Downloads are kept in `.cache/sources/` and reused. An already processed video is skipped unless
you pass `--force`.

## Decisions

| Decision | Why |
|---|---|
| Ladder 1080p / 720p / 540p / 360p (5000k / 2800k / 1600k / 800k) | Steps of ~1.6–1.8×. In lab 04 a 3× jump meant ABR never visited one quality |
| Each quality is a **box** (1920×1080, 1280×720…) the video fits in without distortion | A 1920×818 widescreen film is "1080p". Comparing height alone, it lost its 1080p rung |
| **Never upscale** | A 480p source produces 480p + 360p. Inventing a "1080p" from SD costs bandwidth without adding quality |
| 480p only for SD sources; odd SD sources (608×448) add their native size as the top rung | With HD sources 480p would sit right next to 540p; and shrinking 448p to 360p would throw away resolution |
| 4 s segments, a keyframe every 2 s by **time** (`-force_key_frames`) | Aligned across qualities regardless of the source frame rate (24, 25, 29.97, 30) |
| `sc_threshold 0` | Prevents extra keyframes on scene cuts, which would misalign a quality |
| `BANDWIDTH` = measured peak of real segments, `AVERAGE-BANDWIDTH` = mean | `BANDWIDTH` must be ≥ the real peak (video + audio + TS container) |
| No audio if the source has none | Silent shorts don't carry an empty track, and `CODECS` doesn't announce `mp4a` |
| `setsar=1` | Some SD sources don't have square pixels; every quality comes out square |

Posters from 4:3 sources (silent films) come out 4:3: the app has to crop them (`ContentScale.Crop`).

## Content and licenses

| Video | License | Max source |
|---|---|---|
| Big Buck Bunny, Sintel, Tears of Steel | CC BY 3.0 — attribution required | 1080p (Sintel and ToS widescreen) |
| Elephants Dream | CC BY 2.5 — attribution required | 576p |
| Night of the Living Dead (1968) | Public domain | 480p |
| The General (1926) | Public domain (US) — served muted: the added music may be copyrighted | 432p (cropped) |
| The Immigrant (1917) | Public domain (US) — no audio | 430p (deinterlaced, cropped) |

Each video's credit text is in `attribution` and reaches the app through `catalog.json`.

Optional per-video fields: `poster_seconds` (poster frame), `deinterlace`, `crop` (FFmpeg
`w:h:x:y`, removes baked-in black borders) and `mute` (drops the audio track).

A quality's name (`1080p`, `720p`…) is the **box**, not the actual height: *Sintel*'s `1080p` is
1920×818. If the app labels qualities, it should use the rendition's `name`, not the short side.
