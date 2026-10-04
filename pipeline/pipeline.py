#!/usr/bin/env python3
"""Buffered pipeline: video fuente -> escalera HLS + póster + metadatos + catálogo.

Para cada video de videos.json:
  1. Descarga la fuente a .cache/sources/ (y la extrae si es .zip). Se reutiliza si ya existe.
  2. Inspecciona la fuente con ffprobe (resolución, fps, duración, si trae audio).
  3. Codifica las calidades de la escalera que NO superen la resolución de la fuente
     (no se inventan calidades: una fuente de 480p genera una escalera hasta 480p).
  4. Escribe master.m3u8 con BANDWIDTH/AVERAGE-BANDWIDTH medidos de los segmentos reales.
  5. Genera poster.jpg (miniatura) y backdrop.jpg (imagen grande para el detalle).
  6. Escribe metadata.json del video y, al final, media/catalog.json con todo el catálogo.

Uso:
  python3 pipeline/pipeline.py                      # todos los videos
  python3 pipeline/pipeline.py big-buck-bunny       # solo algunos (por id)
  python3 pipeline/pipeline.py --max-seconds 60     # solo los primeros 60 s (pruebas rápidas)
  python3 pipeline/pipeline.py --force              # rehace aunque ya exista la salida

Solo usa la biblioteca estándar de Python + ffmpeg/ffprobe.
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

RAIZ = Path(__file__).resolve().parent.parent
VIDEOS_JSON = RAIZ / "pipeline" / "videos.json"
CACHE = RAIZ / ".cache" / "sources"
MEDIA = RAIZ / "media"

# Escalera ABR como "cajas": cada calidad es el tamaño máximo en el que cabe el video respetando su
# proporción. Así una película panorámica de 1920×818 es "1080p" (1920×818) y no se queda sin escalón,
# como pasaría comparando solo el alto. Escalones de ~1.6-1.8x entre calidades (lección del lab 04/05:
# con saltos de 3x el ABR se salta calidades).
ESCALERA = [
    # nombre, caja ancho, caja alto, bitrate video, nivel H.264, código CODECS
    ("1080p", 1920, 1080, "5000k", "4.0", "avc1.640028"),
    ("720p",  1280,  720, "2800k", "3.1", "avc1.64001f"),
    ("540p",   960,  540, "1600k", "3.1", "avc1.64001f"),
    ("480p",   854,  480, "1200k", "3.0", "avc1.64001e"),
    ("360p",   640,  360,  "800k", "3.0", "avc1.64001e"),
]
SEGMENTO_S = 4          # duración de cada segmento HLS
GOP_S = 2               # un keyframe cada 2 s, alineado en todas las calidades
AUDIO = ["-c:a", "aac", "-b:a", "128k", "-ac", "2", "-ar", "48000"]
CODEC_AUDIO = "mp4a.40.2"


def log(msg):
    print(f"[{time.strftime('%H:%M:%S')}] {msg}", flush=True)


def correr(cmd):
    resultado = subprocess.run(cmd, stdin=subprocess.DEVNULL, capture_output=True, text=True)
    if resultado.returncode != 0:
        sys.stderr.write(resultado.stderr[-3000:])
        raise RuntimeError(f"Falló: {' '.join(cmd[:6])} ...")
    return resultado.stdout


# --- 1. Descarga ------------------------------------------------------------------------------

def descargar(video):
    CACHE.mkdir(parents=True, exist_ok=True)
    url = video["source_url"]
    nombre = urllib.request.unquote(url.rsplit("/", 1)[-1])
    destino = CACHE / nombre
    if not destino.exists():
        log(f"  descargando {nombre} ...")
        parcial = destino.with_suffix(destino.suffix + ".part")
        # Algunos servidores (download.blender.org) rechazan con 403 el User-Agent por defecto de urllib.
        peticion = urllib.request.Request(url, headers={"User-Agent": "buffered-pipeline/1.0 (+ffmpeg)"})
        with urllib.request.urlopen(peticion) as resp, open(parcial, "wb") as f:
            shutil.copyfileobj(resp, f, length=1024 * 1024)
        parcial.rename(destino)
    if "zip_member" not in video:
        return destino
    extraido = CACHE / video["zip_member"]
    if not extraido.exists():
        log(f"  extrayendo {video['zip_member']} ...")
        with zipfile.ZipFile(destino) as z:
            miembro = next(n for n in z.namelist() if n.endswith(video["zip_member"]))
            with z.open(miembro) as src, open(extraido, "wb") as dst:
                shutil.copyfileobj(src, dst, length=1024 * 1024)
    return extraido


# --- 2. Inspección ----------------------------------------------------------------------------

def inspeccionar(fuente):
    datos = json.loads(correr([
        "ffprobe", "-v", "error", "-print_format", "json",
        "-show_streams", "-show_format", str(fuente),
    ]))
    video = next(s for s in datos["streams"] if s["codec_type"] == "video")
    num, den = (int(x) for x in video.get("avg_frame_rate", "0/1").split("/"))
    fps = num / den if den else 0
    if not fps:
        num, den = (int(x) for x in video["r_frame_rate"].split("/"))
        fps = num / den
    # Ancho visible considerando el aspecto de pixel (SAR): algunas fuentes SD no son píxel cuadrado.
    sar = video.get("sample_aspect_ratio", "1:1")
    sn, sd = (int(x) for x in sar.split(":")) if sar not in ("0:1", "N/A") else (1, 1)
    ancho = round(video["width"] * sn / sd)
    return {
        "ancho": ancho,
        "alto": video["height"],
        "fps": fps,
        "duracion": float(datos["format"]["duration"]),
        "tiene_audio": any(s["codec_type"] == "audio" for s in datos["streams"]),
    }


# --- 3. Codificación de la escalera -----------------------------------------------------------

def elegir_escalones(info):
    """Calidades a generar: las cajas que la fuente llena en ancho o en alto. Nunca se escala hacia arriba."""
    sw, sh = info["ancho"], info["alto"]
    hd = sw >= 960 or sh >= 540
    escalones = [e for e in ESCALERA if sw >= e[1] or sh >= e[2]]
    if hd:
        # 480p solo existe para fuentes SD: con fuentes HD quedaría pegado a 540p.
        escalones = [e for e in escalones if e[0] != "480p"]
    elif not escalones or sh > escalones[0][2]:
        # Fuente SD rara (p. ej. 608×448): se agrega su resolución nativa como calidad máxima,
        # para no tirar resolución bajándola a 360p.
        escalones.insert(0, (f"{sh}p", sw, sh, "1200k", "3.0", "avc1.64001e"))
    return escalones or [ESCALERA[-1]]


def codificar(fuente, info, salida, max_segundos):
    resultado = []
    for nombre, caja_w, caja_h, bitrate, nivel, codecs in elegir_escalones(info):
        carpeta = salida / nombre
        carpeta.mkdir(parents=True, exist_ok=True)
        log(f"  codificando {nombre} ({bitrate}) ...")
        bufsize = f"{int(bitrate[:-1]) * 2}k"
        cmd = ["ffmpeg", "-hide_banner", "-y", "-i", str(fuente)]
        if max_segundos:
            cmd += ["-t", str(max_segundos)]
        cmd += [
            "-map", "0:v:0",
            *(["-map", "0:a:0"] if info["tiene_audio"] else []),
            # Cabe en la caja sin deformar; dimensiones pares (H.264 4:2:0); píxel cuadrado.
            "-vf", (f"scale=w={caja_w}:h={caja_h}:force_original_aspect_ratio=decrease:flags=lanczos,"
                    "scale=trunc(iw/2)*2:trunc(ih/2)*2,setsar=1,format=yuv420p"),
            "-c:v", "libx264", "-preset", "veryfast", "-profile:v", "high", "-level", nivel,
            "-b:v", bitrate, "-maxrate", bitrate, "-bufsize", bufsize,
            # Keyframe cada GOP_S segundos por tiempo, no por número de frames: así queda alineado
            # entre calidades sin importar los fps de la fuente. sc_threshold 0 evita keyframes extra.
            "-force_key_frames", f"expr:gte(t,n_forced*{GOP_S})", "-sc_threshold", "0",
            *(AUDIO if info["tiene_audio"] else ["-an"]),
            "-f", "hls", "-hls_time", str(SEGMENTO_S), "-hls_playlist_type", "vod",
            "-hls_segment_filename", str(carpeta / "seg_%04d.ts"),
            str(carpeta / "index.m3u8"),
        ]
        correr(cmd)
        pico, promedio, ancho_real, alto_real = medir(carpeta)
        resultado.append({
            "name": nombre, "width": ancho_real, "height": alto_real,
            "peakBandwidth": pico, "averageBandwidth": promedio,
            "codecs": codecs + (f",{CODEC_AUDIO}" if info["tiene_audio"] else ""),
            "playlist": f"{nombre}/index.m3u8",
        })
    return resultado


def medir(carpeta):
    """Bitrate pico y promedio reales (bits/s) a partir de los segmentos y sus duraciones."""
    lineas = (carpeta / "index.m3u8").read_text().splitlines()
    pico = total_bits = total_s = 0
    dur = None
    for l in lineas:
        if l.startswith("#EXTINF:"):
            dur = float(l[8:].split(",")[0])
        elif l.endswith(".ts") and dur:
            bits = (carpeta / l).stat().st_size * 8
            pico = max(pico, bits / dur)
            total_bits += bits
            total_s += dur
    primer_seg = next(l for l in lineas if l.endswith(".ts"))
    stream = json.loads(correr([
        "ffprobe", "-v", "error", "-select_streams", "v:0", "-show_entries", "stream=width,height",
        "-print_format", "json", str(carpeta / primer_seg),
    ]))["streams"][0]
    # Redondeo hacia arriba a 10 kb/s: BANDWIDTH debe ser >= el pico real.
    return math.ceil(pico / 10_000) * 10_000, round(total_bits / total_s), stream["width"], stream["height"]


def escribir_maestra(salida, calidades):
    lineas = ["#EXTM3U", "#EXT-X-VERSION:3", "#EXT-X-INDEPENDENT-SEGMENTS"]
    for c in calidades:  # de mayor a menor, como en ESCALERA
        lineas.append(
            f"#EXT-X-STREAM-INF:BANDWIDTH={c['peakBandwidth']},AVERAGE-BANDWIDTH={c['averageBandwidth']},"
            f"RESOLUTION={c['width']}x{c['height']},CODECS=\"{c['codecs']}\""
        )
        lineas.append(c["playlist"])
    (salida / "master.m3u8").write_text("\n".join(lineas) + "\n")


# --- 4. Imágenes ------------------------------------------------------------------------------

def imagenes(video, fuente, info, salida, max_segundos):
    duracion = min(info["duracion"], max_segundos) if max_segundos else info["duracion"]
    # "poster_seconds" en videos.json elige el cuadro a mano; si no, el 20% suele evitar
    # créditos y negros del inicio.
    instante = video.get("poster_seconds")
    if instante is None or instante >= duracion:
        instante = duracion * 0.2
    instante = f"{instante:.1f}"
    for nombre, ancho in (("poster.jpg", 640), ("backdrop.jpg", 1280)):
        correr([
            "ffmpeg", "-hide_banner", "-y", "-ss", instante, "-i", str(fuente), "-frames:v", "1",
            "-vf", f"scale={ancho}:-2:flags=lanczos,setsar=1", "-q:v", "3", str(salida / nombre),
        ])


# --- Orquestación -----------------------------------------------------------------------------

def procesar(video, max_segundos, forzar):
    salida = MEDIA / video["id"]
    meta_path = salida / "metadata.json"
    if meta_path.exists() and not forzar:
        log(f"{video['id']}: ya existe, se reutiliza (usa --force para rehacer)")
        return json.loads(meta_path.read_text())
    log(f"{video['id']}: procesando")
    fuente = descargar(video)
    info = inspeccionar(fuente)
    log(f"  fuente: {info['ancho']}x{info['alto']} @ {info['fps']:.2f} fps, "
        f"{info['duracion'] / 60:.1f} min, audio={'sí' if info['tiene_audio'] else 'no'}")
    if salida.exists():
        shutil.rmtree(salida)
    salida.mkdir(parents=True)
    calidades = codificar(fuente, info, salida, max_segundos)
    escribir_maestra(salida, calidades)
    imagenes(video, fuente, info, salida, max_segundos)
    duracion = min(info["duracion"], max_segundos) if max_segundos else info["duracion"]
    meta = {
        "id": video["id"],
        "title": video["title"],
        "year": video["year"],
        "description": video["description"],
        "durationSeconds": round(duracion),
        "license": video["license"],
        "attribution": video["attribution"],
        "sourceUrl": video["source_url"],
        "master": "master.m3u8",
        "poster": "poster.jpg",
        "backdrop": "backdrop.jpg",
        "renditions": calidades,
        "trimmedForTesting": bool(max_segundos),
    }
    meta_path.write_text(json.dumps(meta, indent=2, ensure_ascii=False) + "\n")
    log(f"  listo: {', '.join(c['name'] for c in calidades)}")
    return meta


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("ids", nargs="*", help="ids de videos a procesar (por defecto, todos)")
    p.add_argument("--max-seconds", type=int, default=0, help="procesar solo los primeros N segundos")
    p.add_argument("--force", action="store_true", help="rehacer aunque ya exista la salida")
    args = p.parse_args()

    for herramienta in ("ffmpeg", "ffprobe"):
        if not shutil.which(herramienta):
            sys.exit(f"Falta {herramienta} en el PATH")

    videos = json.loads(VIDEOS_JSON.read_text())["videos"]
    if args.ids:
        desconocidos = set(args.ids) - {v["id"] for v in videos}
        if desconocidos:
            sys.exit(f"ids desconocidos: {', '.join(sorted(desconocidos))}")
        videos = [v for v in videos if v["id"] in args.ids]

    for v in videos:
        procesar(v, args.max_seconds, args.force)

    # El catálogo siempre se reconstruye con todo lo que haya en media/, en el orden de videos.json.
    orden = [v["id"] for v in json.loads(VIDEOS_JSON.read_text())["videos"]]
    entradas = [json.loads((MEDIA / i / "metadata.json").read_text())
                for i in orden if (MEDIA / i / "metadata.json").exists()]
    catalogo = {"generatedAt": datetime.now(timezone.utc).isoformat(timespec="seconds"), "videos": entradas}
    (MEDIA / "catalog.json").write_text(json.dumps(catalogo, indent=2, ensure_ascii=False) + "\n")
    log(f"catalog.json: {len(entradas)} videos")


if __name__ == "__main__":
    main()
