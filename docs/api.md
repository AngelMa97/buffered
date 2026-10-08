# Buffered API (server ⇄ app contract)

This document is the agreement between `server/` and `app/`. If something changes here, both change.
All responses are UTF-8 JSON. URLs returned by the API are **absolute** and built from the host the
client used (`10.0.2.2` from the emulator, `localhost` through `adb reverse`, the LAN IP…).

## `GET /health`

```json
{ "status": "ok", "videos": 7 }
```

## `GET /api/videos`

List for the catalog grid, in the order of `pipeline/videos.json`.

```json
[
  {
    "id": "big-buck-bunny",
    "title": "Big Buck Bunny",
    "year": 2008,
    "durationSeconds": 635,
    "posterUrl": "http://10.0.2.2:8080/media/big-buck-bunny/poster.jpg"
  }
]
```

## `GET /api/videos/{id}`

Detail for the detail screen and the player.

```json
{
  "id": "big-buck-bunny",
  "title": "Big Buck Bunny",
  "year": 2008,
  "description": "A giant, gentle rabbit …",
  "durationSeconds": 635,
  "posterUrl": "http://10.0.2.2:8080/media/big-buck-bunny/poster.jpg",
  "backdropUrl": "http://10.0.2.2:8080/media/big-buck-bunny/backdrop.jpg",
  "streamUrl": "http://10.0.2.2:8080/media/big-buck-bunny/master.m3u8",
  "license": { "name": "CC BY 3.0", "url": "https://creativecommons.org/licenses/by/3.0/" },
  "attribution": "(c) copyright 2008, Blender Foundation / www.bigbuckbunny.org",
  "renditions": [
    { "name": "1080p", "width": 1920, "height": 1080, "bandwidth": 5870000 },
    { "name": "360p",  "width": 640,  "height": 360,  "bandwidth": 1110000 }
  ]
}
```

`renditions` is informational (for example, to show "HD" on the detail screen). The player does
**not** need it: it reads the actual qualities from `master.m3u8`.

If the id doesn't exist: **`404`** with

```json
{ "error": "video_not_found", "message": "No video with id 'xyz'" }
```

## `GET /media/{id}/…`

Files produced by the pipeline: `master.m3u8`, `{quality}/index.m3u8`, `{quality}/seg_NNNN.ts`,
`poster.jpg`, `backdrop.jpg`. Content types:

| Extension | Content-Type |
|---|---|
| `.m3u8` | `application/vnd.apple.mpegurl` |
| `.ts` | `video/mp2t` |
| `.jpg` | `image/jpeg` |

## Demo mode (optional)

`PUT /demo/throttle?mbps=1.5` caps the speed of `/media/**` to trigger ABR live; `mbps=0` removes
the cap. Only available when the server starts with demo mode enabled (`BUFFERED_DEMO=true`).
