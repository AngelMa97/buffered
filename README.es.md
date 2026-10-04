# Buffered

[English](README.md) · **Español**

> *Una plataforma de streaming de punta a punta que te deja ver lo que pasa por dentro.*

![Cómo funciona Buffered](docs/assets/buffered-architecture.gif)

🚧 **En construcción.** El pipeline de codificación está listo; el servidor y la app Android están en desarrollo.

## Por qué existe

Hace unas semanas vi una vacante de una empresa grande del mundo del streaming. Un requisito me llamó la
atención: *"Buen conocimiento de procesamiento de video"*. Me surgieron muchas preguntas, pero la más grande
fue: **¿cómo se procesa realmente el video en una app de Android?**

Así que decidí aprender cómo funciona el video, usando la IA como maestra y enfocándome en laboratorios
prácticos más que en teoría: inspeccionar streams con FFmpeg, medir la compresión, construir una escalera de
bitrate adaptativo y, al final, reproducir mi propio stream HLS en un dispositivo real y ver al reproductor
cambiar de calidad según cambiaba la red.

Al terminar ese laboratorio creé este repo para juntar todo lo que aprendí con mi experiencia en desarrollo
Android, de punta a punta: el pipeline de codificación, el servidor y la app.

## Cómo funciona

1. **Pipeline** — FFmpeg convierte cada película fuente en una **escalera de bitrate** HLS
   (1080p / 720p / 540p / 360p) con keyframes alineados entre calidades, además de pósters y un catálogo.
2. **Servidor** — Ktor sirve el catálogo como API REST y los segmentos HLS como archivos. Un modo demo
   puede limitar la red a voluntad.
3. **App** — Android con Media3 (ExoPlayer): catálogo, detalle y reproductor. Un panel **"Stats for nerds"**
   muestra lo que hace el reproductor: la calidad que se descarga contra la que se ve, la estimación de
   ancho de banda y el buffer.

| Carpeta | Qué es |
|---|---|
| [`pipeline/`](pipeline/) | FFmpeg: video fuente → escalera HLS + pósters + catálogo |
| [`server/`](server/) | Ktor: API del catálogo + streams HLS |
| [`app/`](app/) | Android: catálogo → detalle → reproductor (MVI, módulos por feature y capa) |
| [`docs/`](docs/) | Contrato de la API y decisiones |

## Contenido y licencias

Todos los videos son Creative Commons o de dominio público:
*Big Buck Bunny*, *Sintel*, *Tears of Steel* y *Elephants Dream* © Blender Foundation (CC BY), y
*Night of the Living Dead* (1968), *The General* (1926) y *The Immigrant* (1917) (dominio público).
Los créditos completos están en [`pipeline/videos.json`](pipeline/videos.json) y dentro de la app.
