#!/usr/bin/env python3
"""Genera la animación "cómo funciona Buffered" (MP4 para LinkedIn + GIF para el README).

    python3 docs/assets/make_animation.py

Requiere Pillow y ffmpeg. Fuentes: Inter (OFL) y JetBrains Mono en ~/.cache/buffered-fonts/
(Inter-{Regular,Medium,SemiBold,Bold}.ttf, JetBrainsMono-Medium.ttf). La imagen del teléfono sale de
media/big-buck-bunny/backdrop.jpg (corre el pipeline antes).

La red y el reproductor se simulan con reglas simples pero fieles a lo medido en los labs:
el ABR elige calidad al pedir cada segmento con ~70% del ancho de banda estimado, y lo que se ve en
pantalla va atrasado respecto a lo que se descarga por el buffer.
"""
import math
import shutil
import subprocess
import tempfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

RAIZ = Path(__file__).resolve().parents[2]
SALIDA = Path(__file__).resolve().parent
FUENTES = Path.home() / ".cache" / "buffered-fonts"
FONDO_VIDEO = RAIZ / "media" / "big-buck-bunny" / "backdrop.jpg"

W, H = 1600, 900
SS = 2                      # supersampling para bordes suaves
FPS = 30
DURACION = 16.0
N = int(DURACION * FPS)

# Paleta (tema oscuro)
BG = (10, 14, 20)
GRID = (20, 27, 37)
CARD = (18, 24, 33)
CARD_BORDE = (40, 50, 64)
TEXTO = (232, 238, 245)
TENUE = (138, 152, 170)
ACENTO = (45, 212, 191)     # teal: identidad de Buffered
CALIDAD_COLOR = {
    "1080p": (52, 211, 153),
    "720p": (96, 165, 250),
    "540p": (251, 191, 36),
    "360p": (248, 113, 113),
}
ESCALERA = [("1080p", 5.0), ("720p", 2.8), ("540p", 1.6), ("360p", 0.8)]


def fuente(nombre, tam):
    return ImageFont.truetype(str(FUENTES / nombre), tam * SS)


F = {
    "titulo": fuente("Inter-Bold.ttf", 34),
    "sub": fuente("Inter-Regular.ttf", 19),
    "card": fuente("Inter-SemiBold.ttf", 19),
    "etq": fuente("Inter-Medium.ttf", 13),
    "cuerpo": fuente("Inter-Regular.ttf", 16),
    "cuerpo_b": fuente("Inter-SemiBold.ttf", 16),
    "mono": fuente("JetBrainsMono-Medium.ttf", 14),
    "mono_s": fuente("JetBrainsMono-Medium.ttf", 12),
    "caption": fuente("Inter-SemiBold.ttf", 26),
    "caption_n": fuente("Inter-Bold.ttf", 26),
    "seg": fuente("Inter-Bold.ttf", 11),
}


# --- utilidades de animación -----------------------------------------------------------------

def clamp(x, a=0.0, b=1.0):
    return max(a, min(b, x))


def ease(x):
    x = clamp(x)
    return x * x * (3 - 2 * x)


def tramo(t, ini, fin):
    """0→1 entre ini y fin con suavizado."""
    return ease((t - ini) / (fin - ini)) if fin > ini else float(t >= ini)


def mezcla(c, alfa, fondo=BG):
    return tuple(round(fondo[i] + (c[i] - fondo[i]) * alfa) for i in range(3))


def s(*v):
    return [round(x * SS) for x in v]


# --- simulación de red y reproductor ---------------------------------------------------------

def ancho_banda(t):
    """Mb/s disponibles: alto, caída fuerte, recuperación."""
    if t < 8.6:
        return 9.0
    if t < 9.3:
        return 9.0 + (1.3 - 9.0) * ease((t - 8.6) / 0.7)
    if t < 12.0:
        return 1.3
    if t < 12.7:
        return 1.3 + (9.0 - 1.3) * ease((t - 12.0) / 0.7)
    return 9.0


INICIO_STREAM = 6.0
PERIODO_SEG = 0.55   # cada cuánto pide un segmento (comprimido para la animación)


def simular():
    """Calcula por segmento: momento de pedido, calidad elegida y momento en que se ve."""
    segs = []
    estimado = 4.0                     # el estimador arranca conservador
    t = INICIO_STREAM
    while t < DURACION + 2:
        bw = ancho_banda(t)
        estimado += (bw - estimado) * 0.55        # se adapta en pocos segmentos
        presupuesto = estimado * 0.7              # la regla del 70%
        calidad = next((n for n, br in ESCALERA if br <= presupuesto), "360p")
        # Subir es más lento que bajar (ExoPlayer exige más buffer para subir):
        if segs:
            orden = [n for n, _ in ESCALERA]
            previa = segs[-1]["q"]
            if orden.index(calidad) < orden.index(previa) - 1:
                calidad = orden[orden.index(previa) - 1]
        viaje = clamp(0.25 + 1.6 / max(bw, 0.5), 0.35, 1.6)
        segs.append({"pide": t, "q": calidad, "viaje": viaje, "llega": t + viaje})
        t += PERIODO_SEG
    # Lo que se ve va ~4 segmentos detrás de lo descargado (el buffer)
    atraso = 2
    for i, sg in enumerate(segs):
        sg["visible"] = segs[max(0, i - atraso)]["q"] if i >= atraso else "360p"
    return segs


SEGS = simular()


def estado(t):
    llegados = [sg for sg in SEGS if sg["llega"] <= t]
    descargando = next((sg["q"] for sg in reversed(SEGS) if sg["pide"] <= t), "—")
    visible = llegados[-1]["visible"] if llegados else "—"
    # buffer: segundos "en tanque"; baja cuando la red cae
    if t < INICIO_STREAM + 0.8:
        buf = 0.0
    else:
        buf = 22.0
        if t > 9.0:
            buf -= min(t - 9.0, 2.6) * 5.5
        if t > 12.3:
            buf += min(t - 12.3, 3.0) * 4.6
    return descargando, visible, clamp(buf, 0, 30), llegados


# --- dibujo ----------------------------------------------------------------------------------

def tarjeta(d, x, y, w, h, titulo, alfa, icono=None):
    borde = mezcla(CARD_BORDE, alfa)
    d.rounded_rectangle(s(x, y, x + w, y + h), radius=18 * SS, fill=mezcla(CARD, alfa), outline=borde, width=2 * SS)
    d.text(s(x + 22, y + 18), titulo, font=F["etq"], fill=mezcla(ACENTO, alfa))


def flecha(d, x1, y, x2, alfa, pulso=None):
    col = mezcla((70, 84, 104), alfa)
    d.line(s(x1, y, x2 - 10, y), fill=col, width=3 * SS)
    d.polygon(s(x2, y, x2 - 12, y - 7, x2 - 12, y + 7), fill=col)
    if pulso is not None and 0 <= pulso <= 1:
        px = x1 + (x2 - x1) * pulso
        r = 7
        d.ellipse(s(px - r, y - r, px + r, y + r), fill=mezcla(ACENTO, alfa))


def texto_centrado(d, cx, y, txt, f, col):
    w = d.textlength(txt, font=f) / SS
    d.text(s(cx - w / 2, y), txt, font=f, fill=col)


def cuadro_video(base, calidad, ancho, alto):
    """Imagen del teléfono degradada según la calidad que se ve (exagerado para que se note)."""
    factor = {"1080p": 1.0, "720p": 0.42, "540p": 0.24, "360p": 0.11, "—": 0.11}[calidad]
    img = base.resize((ancho, alto), Image.LANCZOS)
    if factor < 1:
        chica = img.resize((max(8, int(ancho * factor)), max(4, int(alto * factor))), Image.BILINEAR)
        img = chica.resize((ancho, alto), Image.NEAREST).filter(ImageFilter.GaussianBlur(0.6 * SS))
    return img


def cuadro(t, base_video):
    img = Image.new("RGB", (W * SS, H * SS), BG)
    d = ImageDraw.Draw(img)

    # retícula sutil
    for gx in range(0, W, 40):
        d.line(s(gx, 0, gx, H), fill=GRID, width=1)
    for gy in range(0, H, 40):
        d.line(s(0, gy, W, gy), fill=GRID, width=1)

    fin = 1 - tramo(t, DURACION - 0.6, DURACION)   # fundido al final para el loop

    # Encabezado
    a = tramo(t, 0.0, 0.6) * fin
    d.text(s(64, 46), "Buffered", font=F["titulo"], fill=mezcla(TEXTO, a))
    d.text(s(66, 92), "How a film becomes an adaptive stream on your phone", font=F["sub"], fill=mezcla(TENUE, a))

    Y, HC = 170, 470   # fila de tarjetas

    # 1. Fuente
    a1 = tramo(t, 0.4, 1.0) * fin
    tarjeta(d, 64, Y, 230, HC, "SOURCE", a1)
    # ícono de película
    fx, fy = 104, Y + 70
    d.rounded_rectangle(s(fx, fy, fx + 150, fy + 100), radius=10 * SS, outline=mezcla(TEXTO, a1), width=3 * SS)
    for k in range(5):
        d.rectangle(s(fx + 10 + k * 28, fy + 8, fx + 26 + k * 28, fy + 18), fill=mezcla(TENUE, a1))
        d.rectangle(s(fx + 10 + k * 28, fy + 82, fx + 26 + k * 28, fy + 92), fill=mezcla(TENUE, a1))
    d.polygon(s(fx + 62, fy + 34, fx + 62, fy + 66, fx + 92, fy + 50), fill=mezcla(ACENTO, a1))
    d.text(s(88, Y + 200), "big_buck_bunny.mp4", font=F["mono"], fill=mezcla(TEXTO, a1))
    d.text(s(88, Y + 226), "1920×1080 · 30 fps", font=F["cuerpo"], fill=mezcla(TENUE, a1))
    d.text(s(88, Y + 290), "Creative Commons /", font=F["cuerpo"], fill=mezcla(TENUE, a1))
    d.text(s(88, Y + 314), "public domain films", font=F["cuerpo"], fill=mezcla(TENUE, a1))

    flecha(d, 300, Y + HC / 2, 350, tramo(t, 1.0, 1.4) * fin, pulso=(t - 1.6) / 0.6)

    # 2. Pipeline con la escalera
    a2 = tramo(t, 1.2, 1.8) * fin
    tarjeta(d, 356, Y, 300, HC, "PIPELINE · FFmpeg", a2)
    d.text(s(378, Y + 50), "Bitrate ladder", font=F["card"], fill=mezcla(TEXTO, a2))
    for i, (q, br) in enumerate(ESCALERA):
        crece = tramo(t, 2.1 + i * 0.35, 2.7 + i * 0.35) * fin
        by = Y + 100 + i * 62
        d.text(s(378, by + 4), q, font=F["cuerpo_b"], fill=mezcla(TEXTO, a2))
        largo = 128 * br / 5.0 * crece
        d.rounded_rectangle(s(448, by + 2, 448 + max(largo, 1), by + 26), radius=6 * SS,
                            fill=mezcla(CALIDAD_COLOR[q], a2 * (0.25 + 0.75 * crece)))
        d.text(s(452 + max(largo, 1) + 6, by + 5), f"{br:.1f} Mb/s", font=F["mono_s"], fill=mezcla(TENUE, a2 * crece))
    d.text(s(378, Y + 360), "Keyframes every 2 s,", font=F["cuerpo"], fill=mezcla(TENUE, a2))
    d.text(s(378, Y + 384), "aligned across qualities", font=F["cuerpo"], fill=mezcla(TENUE, a2))
    d.text(s(378, Y + 416), "→ 4 s HLS segments", font=F["cuerpo_b"], fill=mezcla(TEXTO, a2))

    flecha(d, 662, Y + HC / 2, 712, tramo(t, 3.6, 4.0) * fin, pulso=(t - 4.0) / 0.6)

    # 3. Servidor
    a3 = tramo(t, 3.8, 4.4) * fin
    tarjeta(d, 718, Y, 260, HC, "SERVER · Ktor", a3)
    filas = [
        ("GET /api/videos", 4.6), ("GET /api/videos/{id}", 4.85),
        ("GET /media/…/master.m3u8", 5.1), ("GET /media/…/720p/index.m3u8", 5.35),
    ]
    for i, (txt, ta) in enumerate(filas):
        af = tramo(t, ta, ta + 0.3) * a3
        d.text(s(740, Y + 56 + i * 30), txt, font=F["mono_s"], fill=mezcla(TEXTO if i < 2 else TENUE, af))
    # segmentos servidos (los últimos pedidos)
    pedidos = [sg for sg in SEGS if sg["pide"] <= t][-6:]
    for i, sg in enumerate(pedidos):
        yy = Y + 200 + i * 28
        col = CALIDAD_COLOR[sg["q"]]
        d.rounded_rectangle(s(740, yy, 752, yy + 12), radius=3 * SS, fill=mezcla(col, a3))
        idx = SEGS.index(sg)
        d.text(s(760, yy - 3), f"{sg['q']}/seg_{idx:04d}.ts", font=F["mono_s"], fill=mezcla(TEXTO, a3))
    d.text(s(740, Y + 384), "Catalog API + HLS files", font=F["cuerpo"], fill=mezcla(TENUE, a3))
    d.text(s(740, Y + 408), "Demo mode: throttle", font=F["cuerpo"], fill=mezcla(TENUE, a3))
    d.text(s(740, Y + 430), "the network on demand", font=F["cuerpo"], fill=mezcla(TENUE, a3))

    # 4. Red: canal con segmentos viajando + medidor
    a4 = tramo(t, 5.2, 5.8) * fin
    cx1, cx2, cy = 990, 1222, Y + HC / 2
    d.line(s(cx1, cy, cx2, cy), fill=mezcla((50, 62, 78), a4), width=24 * SS)
    for sg in SEGS:
        if sg["pide"] <= t < sg["llega"]:
            p = (t - sg["pide"]) / sg["viaje"]
            px = cx1 + 14 + (cx2 - cx1 - 28) * p
            col = CALIDAD_COLOR[sg["q"]]
            d.rounded_rectangle(s(px - 22, cy - 11, px + 22, cy + 11), radius=5 * SS, fill=mezcla(col, a4))
            texto_centrado(d, px, cy - 8, sg["q"], F["seg"], mezcla(BG, a4))
    bw = ancho_banda(t)
    texto_centrado(d, (cx1 + cx2) / 2, cy - 64, "NETWORK", F["etq"], mezcla(ACENTO, a4))
    col_bw = CALIDAD_COLOR["1080p"] if bw > 5 else CALIDAD_COLOR["360p"]
    texto_centrado(d, (cx1 + cx2) / 2, cy - 42, f"{bw:4.1f} Mb/s", F["cuerpo_b"], mezcla(col_bw, a4))
    # gráfica del ancho de banda
    gx1, gy1, gw, gh = cx1 + 6, cy + 42, cx2 - cx1 - 12, 70
    d.rounded_rectangle(s(gx1, gy1, gx1 + gw, gy1 + gh), radius=8 * SS, outline=mezcla(CARD_BORDE, a4), width=1 * SS)
    puntos = []
    for k in range(80):
        tt = INICIO_STREAM + (DURACION - INICIO_STREAM) * k / 79
        if tt > t:
            break
        puntos.append((gx1 + 6 + (gw - 12) * k / 79, gy1 + gh - 8 - (gh - 16) * ancho_banda(tt) / 10))
    if len(puntos) > 1 and t > INICIO_STREAM:
        d.line([c * SS for p in puntos for c in p], fill=mezcla(ACENTO, a4), width=3 * SS, joint="curve")

    # 5. Teléfono
    a5 = tramo(t, 5.4, 6.0) * fin
    px, py, pw, ph = 1250, 128, 290, 560
    d.rounded_rectangle(s(px, py, px + pw, py + ph), radius=40 * SS, fill=mezcla((6, 9, 13), a5),
                        outline=mezcla((70, 84, 104), a5), width=4 * SS)
    d.rounded_rectangle(s(px + pw / 2 - 40, py + 14, px + pw / 2 + 40, py + 24), radius=5 * SS, fill=mezcla((40, 48, 60), a5))
    descargando, visible, buf, _ = estado(t)
    vx, vy, vw, vh = px + 14, py + 46, pw - 28, int((pw - 28) * 9 / 16)
    if a5 > 0.01:
        video = cuadro_video(base_video, visible if t > INICIO_STREAM + 0.8 else "360p", vw * SS, vh * SS)
        if a5 < 1:
            video = Image.blend(Image.new("RGB", video.size, BG), video, a5)
        img.paste(video, (vx * SS, vy * SS))
        if visible in CALIDAD_COLOR:
            etq = visible
            ew = d.textlength(etq, font=F["seg"]) / SS + 14
            d.rounded_rectangle(s(vx + vw - ew - 8, vy + 8, vx + vw - 8, vy + 26), radius=5 * SS,
                                fill=mezcla(CALIDAD_COLOR[visible], a5))
            d.text(s(vx + vw - ew - 1, vy + 11), etq, font=F["seg"], fill=BG)
    d.text(s(px + 18, vy + vh + 16), "Big Buck Bunny", font=F["card"], fill=mezcla(TEXTO, a5))
    d.text(s(px + 18, vy + vh + 42), "2008 · CC BY · Blender Foundation", font=F["mono_s"], fill=mezcla(TENUE, a5))

    # Stats for nerds
    sx, sy = px + 14, vy + vh + 82
    d.rounded_rectangle(s(sx, sy, px + pw - 14, sy + 232), radius=12 * SS, fill=mezcla((14, 20, 28), a5),
                        outline=mezcla(CARD_BORDE, a5), width=1 * SS)
    d.text(s(sx + 14, sy + 12), "STATS FOR NERDS", font=F["etq"], fill=mezcla(ACENTO, a5))
    filas_s = [
        ("Downloading", descargando, CALIDAD_COLOR.get(descargando, TEXTO)),
        ("On screen", visible, CALIDAD_COLOR.get(visible, TEXTO)),
        ("Bandwidth", f"{bw:.1f} Mb/s" if t > INICIO_STREAM else "—", TEXTO),
    ]
    for i, (k, v, c) in enumerate(filas_s):
        yy = sy + 44 + i * 34
        d.text(s(sx + 14, yy), k, font=F["cuerpo"], fill=mezcla(TENUE, a5))
        vw_t = d.textlength(v, font=F["cuerpo_b"]) / SS
        d.text(s(px + pw - 28 - vw_t, yy), v, font=F["cuerpo_b"], fill=mezcla(c, a5))
    yy = sy + 150
    d.text(s(sx + 14, yy), "Buffer", font=F["cuerpo"], fill=mezcla(TENUE, a5))
    btxt = f"{buf:4.1f} s"
    bw_t = d.textlength(btxt, font=F["cuerpo_b"]) / SS
    d.text(s(px + pw - 28 - bw_t, yy), btxt, font=F["cuerpo_b"], fill=mezcla(TEXTO, a5))
    bx1, bx2, by = sx + 14, px + pw - 28, yy + 34
    d.rounded_rectangle(s(bx1, by, bx2, by + 12), radius=6 * SS, fill=mezcla((34, 42, 54), a5))
    lleno = (bx2 - bx1) * buf / 30
    if lleno > 2:
        col_b = ACENTO if buf > 8 else CALIDAD_COLOR["540p"]
        d.rounded_rectangle(s(bx1, by, bx1 + lleno, by + 12), radius=6 * SS, fill=mezcla(col_b, a5))

    # Leyenda inferior (pasos)
    pasos = [
        (0.4, 2.0, "1", "Start from an open-licensed film"),
        (2.0, 3.8, "2", "Encode it once into a bitrate ladder with FFmpeg"),
        (3.8, 6.0, "3", "Serve the catalog and HLS segments with Ktor"),
        (6.0, 8.8, "4", "The player picks a quality for every segment it downloads"),
        (8.8, 12.4, "5", "Network drops → next segments in 360p, the buffer keeps playback smooth"),
        (12.4, DURACION, "6", "Network recovers → quality climbs back, step by step"),
    ]
    for ini, fin_p, n, txt in pasos:
        ap = (tramo(t, ini, ini + 0.35) - tramo(t, fin_p - 0.25, fin_p)) if fin_p < DURACION else tramo(t, ini, ini + 0.35)
        ap = clamp(ap) * fin
        if ap <= 0.01:
            continue
        cy0 = 712
        d.ellipse(s(64, cy0, 104, cy0 + 40), fill=mezcla(ACENTO, ap))
        texto_centrado(d, 84, cy0 + 6, n, F["caption_n"], mezcla(BG, ap))
        d.text(s(122, cy0 + 4), txt, font=F["caption"], fill=mezcla(TEXTO, ap))

    # pie
    af = tramo(t, 0.4, 1.0) * fin
    d.text(s(64, 836), "pipeline: FFmpeg  ·  server: Ktor  ·  app: Android + Media3 (ExoPlayer), MVI", font=F["mono_s"],
           fill=mezcla(TENUE, af))
    d.text(s(1340, 836), "github.com/AngelMa97/buffered", font=F["mono_s"], fill=mezcla(TENUE, af))

    return img.resize((W, H), Image.LANCZOS)


def main():
    if not FONDO_VIDEO.exists():
        raise SystemExit("Falta media/big-buck-bunny/backdrop.jpg: corre el pipeline primero")
    base = Image.open(FONDO_VIDEO).convert("RGB")
    tmp = Path(tempfile.mkdtemp(prefix="buffered-anim-"))
    try:
        for i in range(N):
            cuadro(i / FPS, base).save(tmp / f"f_{i:04d}.png")
        mp4 = SALIDA / "buffered-architecture.mp4"
        gif = SALIDA / "buffered-architecture.gif"
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-framerate", str(FPS),
                        "-i", str(tmp / "f_%04d.png"), "-c:v", "libx264", "-preset", "slow", "-crf", "18",
                        "-pix_fmt", "yuv420p", "-movflags", "+faststart", str(mp4)], check=True)
        filtro = ("fps=15,scale=960:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=128:stats_mode=diff[p];"
                  "[b][p]paletteuse=dither=bayer:bayer_scale=4:diff_mode=rectangle")
        subprocess.run(["ffmpeg", "-hide_banner", "-loglevel", "error", "-y", "-i", str(mp4),
                        "-filter_complex", filtro, "-loop", "0", str(gif)], check=True)
        print(f"{mp4.name}: {mp4.stat().st_size / 1e6:.1f} MB · {gif.name}: {gif.stat().st_size / 1e6:.1f} MB")
    finally:
        shutil.rmtree(tmp)


if __name__ == "__main__":
    main()
