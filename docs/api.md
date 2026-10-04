# API de Buffered (contrato servidor ⇄ app)

Este documento es el acuerdo entre `server/` y `app/`. Si cambia algo aquí, cambian los dos.
Todas las respuestas son JSON en UTF-8. Las URLs que devuelve la API son **absolutas** y se arman con
el host que usó el cliente (`10.0.2.2` desde el emulador, `localhost` por `adb reverse`, la IP de la LAN…).

## `GET /health`

```json
{ "status": "ok", "videos": 7 }
```

## `GET /api/videos`

Lista para la cuadrícula del catálogo, en el orden de `pipeline/videos.json`.

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

Detalle para la pantalla de detalle y el reproductor.

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

`renditions` es informativo (por ejemplo, mostrar "HD" en el detalle). El reproductor **no** lo
necesita: las calidades reales las lee de `master.m3u8`.

Si el id no existe: **`404`** con

```json
{ "error": "video_not_found", "message": "No video with id 'xyz'" }
```

## `GET /media/{id}/…`

Archivos que genera el pipeline: `master.m3u8`, `{calidad}/index.m3u8`, `{calidad}/seg_NNNN.ts`,
`poster.jpg`, `backdrop.jpg`. Tipos de contenido:

| Extensión | Content-Type |
|---|---|
| `.m3u8` | `application/vnd.apple.mpegurl` |
| `.ts` | `video/mp2t` |
| `.jpg` | `image/jpeg` |

## Modo demo (opcional, paso 8 del tutorial del servidor)

`PUT /demo/throttle?mbps=1.5` limita la velocidad de `/media/**` para provocar el ABR en vivo;
`mbps=0` quita el límite. Solo existe si el servidor arranca con el modo demo activado.
