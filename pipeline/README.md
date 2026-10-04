# pipeline

Convierte cada video de [`videos.json`](videos.json) en un stream HLS listo para servir:

```
media/
├── catalog.json                 ← catálogo completo (lo que lee el servidor)
└── <id>/
    ├── master.m3u8              ← lista maestra con la escalera
    ├── 1080p/ 720p/ 540p/ 360p/ ← index.m3u8 + seg_0000.ts …
    ├── poster.jpg               ← 640 px de ancho (cuadrícula del catálogo)
    ├── backdrop.jpg             ← 1280 px de ancho (pantalla de detalle)
    └── metadata.json            ← lo mismo que la entrada del video en catalog.json
```

`media/` y `.cache/` están en `.gitignore`: pesan gigas y se regeneran.

## Uso

Requiere `python3` (solo biblioteca estándar) y `ffmpeg`/`ffprobe`. Desde la raíz del repo:

```bash
python3 pipeline/pipeline.py --max-seconds 60   # todo el catálogo, recortado a 60 s (minutos)
python3 pipeline/pipeline.py                    # todo completo (≈5 GB de descarga, tarda)
python3 pipeline/pipeline.py sintel --force     # rehacer un solo video
```

Las descargas se guardan en `.cache/sources/` y se reutilizan. Un video ya procesado se salta salvo
con `--force`.

## Decisiones

| Decisión | Por qué |
|---|---|
| Escalera 1080p / 720p / 540p / 360p (5000k / 2800k / 1600k / 800k) | Escalones de ~1.6–1.8×. En el lab 04 un salto de 3× hizo que el ABR nunca visitara una calidad |
| Cada calidad es una **caja** (1920×1080, 1280×720…) en la que el video cabe sin deformarse | Una película panorámica de 1920×818 es "1080p". Comparando solo el alto, se quedaba sin 1080p |
| **Nunca se escala hacia arriba** | Una fuente de 480p genera 480p + 360p. Inventar un "1080p" desde SD gasta ancho de banda sin ganar calidad |
| 480p solo para fuentes SD; fuentes SD raras (608×448) suman su resolución nativa como tope | Con fuentes HD, 480p quedaría pegado a 540p; y bajar 448p a 360p tiraría resolución |
| Segmentos de 4 s, keyframe cada 2 s por **tiempo** (`-force_key_frames`) | Alineado entre calidades sin importar los fps de la fuente (24, 25, 29.97, 30) |
| `sc_threshold 0` | Evita keyframes extra en cambios de escena que desalinearían una calidad |
| `BANDWIDTH` = pico medido de segmentos reales, `AVERAGE-BANDWIDTH` = promedio | `BANDWIDTH` debe ser ≥ el pico real (video + audio + contenedor TS) |
| Sin audio si la fuente no trae | Los cortos mudos no llevan pista vacía, y `CODECS` no anuncia `mp4a` |
| `setsar=1` | Algunas fuentes SD no son de píxel cuadrado; todas las calidades salen cuadradas |

Los pósters de fuentes 4:3 (cine mudo) salen en 4:3: la app debe recortarlos (`ContentScale.Crop`).

## Contenido y licencias

| Video | Licencia | Fuente máx. |
|---|---|---|
| Big Buck Bunny, Sintel, Tears of Steel | CC BY 3.0 — requiere crédito | 1080p (Sintel y ToS en panorámico) |
| Elephants Dream | CC BY 2.5 — requiere crédito | 576p |
| Night of the Living Dead (1968) | Dominio público | 480p |
| The General (1926) | Dominio público (EE. UU.) — **verificar la música de esta copia** | 448p (nativo) |
| The Immigrant (1917) | Dominio público (EE. UU.) — sin audio | 480p |

El texto de crédito de cada uno está en `attribution` y llega a la app por `catalog.json`.

El nombre de cada calidad (`1080p`, `720p`…) es la **caja**, no el alto real: el `1080p` de *Sintel* mide 1920×818. Si la app etiqueta calidades, que use el `name` de la rendición, no el lado corto.
