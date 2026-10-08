# Buffered

[English](README.md) · **Español**

> *Una plataforma de streaming de punta a punta que te deja ver lo que pasa por dentro.*

![Bitrate adaptativo en acción: el reproductor sube a 1080p, luego cae la red y el buffer se vacía](docs/assets/demo-abr.gif)

*La red se limita a 1.5 Mb/s, se libera y se vuelve a limitar, en vivo desde el servidor. El panel
"Stats for nerds" muestra al reproductor subir de 360p a 1080p y luego seguir eligiendo 1080p mientras el
buffer se vacía hasta cero. [Por qué pasa ↓](#lo-que-reveló-el-panel-de-stats)*

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

![Cómo funciona Buffered](docs/assets/buffered-architecture.gif)

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

## Capturas

| Catálogo | Detalle |
|---|---|
| <img src="docs/assets/screenshots/catalog.jpg" width="260" alt="Cuadrícula del catálogo con pósters"> | <img src="docs/assets/screenshots/detail.jpg" width="260" alt="Pantalla de detalle con licencia y créditos"> |

| Reproductor con "Stats for nerds" | Menú de calidad |
|---|---|
| ![Reproductor con el panel de stats](docs/assets/screenshots/player-stats.jpg) | ![Menú de calidad: Auto, ahorro de datos y calidades fijas](docs/assets/screenshots/quality-menu.jpg) |

## Lo que reveló el panel de stats

**La estimación de ancho de banda va atrasada respecto a la red.** En local el servidor responde a cientos
de Mb/s, así que la estimación de ExoPlayer ronda los 300–500 Mb/s. Cuando el throttle del modo demo baja la
red a 1.5 Mb/s, la estimación tarda unos **18 segundos** en ponerse al día. Mientras tanto "Auto" sigue
eligiendo 1080p (5 Mb/s), el buffer se vacía hasta cero y la reproducción se atora antes de que el reproductor
por fin baje de calidad. Es lo mismo que pasa cuando un celular deja un Wi-Fi rápido por una red móvil débil.

**Fijar una calidad no es lo mismo que ponerle tope.** Elegir una calidad fija es un override de pista:
apaga el bitrate adaptativo y tira lo que ya estaba en el buffer (en la demo, 21 s de buffer cayeron a 0 de
golpe). La opción **Data saving** es un tope (`setMaxVideoSize(1280, 720)`): el reproductor sigue
adaptándose, solo que nunca por encima de 720p.

**"1080p" es una caja, no una altura.** *Sintel* mide 1920×818, así que por su lado corto sería "818p".
Las etiquetas usan la caja estándar más chica en la que cabe el video, así que sigue siendo 1080p.

## Stack técnico

- **App:** Kotlin, Jetpack Compose, Media3 ExoPlayer (HLS, `media3-ui-compose`), Koin, cliente Ktor,
  Coil, Navigation type-safe. MVI en la capa de presentación, módulos por feature y capa, y convention
  plugins de Gradle (AGP 9).
- **Servidor:** Ktor, con un limitador de velocidad compartido en la ruta de media para el modo demo.
- **Pipeline:** Python (solo biblioteca estándar) controlando FFmpeg.
- **Tests:** JUnit 4, AssertK, kotlinx-coroutines-test, `MockEngine` de Ktor y Robolectric.

## Correrlo en local

Necesitas `ffmpeg`, Python 3, un JDK y Android Studio (o solo el SDK de Android).

```bash
# 1. Generar la media (descarga las películas y las recorta a 60 s)
python3 pipeline/pipeline.py --max-seconds 60

# 2. Levantar el servidor (el modo demo activa el throttle de red)
cd server && BUFFERED_DEMO=true ./gradlew run

# 3. Instalar la app (el emulador llega al host en 10.0.2.2:8080)
cd app && ./gradlew installDebug
```

Para limitar la red mientras ves un video: `curl -X PUT "http://localhost:8080/demo/throttle?mbps=1.5"`
(`mbps=0` quita el límite).

En un dispositivo físico, corre `adb reverse tcp:8080 tcp:8080` y agrega `BASE_URL="http://localhost:8080"`
a `app/local.properties`.

Tests: `./gradlew testDebugUnitTest` en `app/` y `./gradlew test` en `server/`.

## Contenido y licencias

Todos los videos son Creative Commons o de dominio público:
*Big Buck Bunny*, *Sintel*, *Tears of Steel* y *Elephants Dream* © Blender Foundation (CC BY), y
*Night of the Living Dead* (1968), *The General* (1926) y *The Immigrant* (1917) (dominio público).
*The General* se sirve sin audio, porque la música agregada a esta copia podría tener copyright.
Los créditos completos están en [`pipeline/videos.json`](pipeline/videos.json) y dentro de la app.
