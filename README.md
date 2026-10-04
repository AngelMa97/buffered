# Buffered

**English** · [Español](README.es.md)

> *An end-to-end streaming platform that shows you what's happening under the hood.*

![How Buffered works](docs/assets/buffered-architecture.gif)

🚧 **Work in progress.** The encoding pipeline is done; the server and the Android app are being built.

## Why this exists

A few weeks ago I came across a job posting from a big company in the streaming space. One requirement
stood out: *"Good video processing knowledge."* It raised a lot of questions, but the biggest one was:
**how is video actually processed in an Android app?**

So I decided to learn how video works, using AI as a teacher and focusing on hands-on labs over theory:
inspecting streams with FFmpeg, measuring compression, building an adaptive bitrate ladder, and finally
playing my own HLS stream on a real device and watching the player switch qualities as the network changed.

After finishing that lab, I created this repo to put everything I learned together with my Android
development experience, end to end: the encoding pipeline, the server, and the app.

## How it works

1. **Pipeline** — FFmpeg turns each source film into an HLS **bitrate ladder** (1080p / 720p / 540p / 360p)
   with keyframes aligned across qualities, plus posters and a catalog.
2. **Server** — Ktor serves the catalog as a REST API and the HLS segments as files. A demo mode can
   throttle the network on demand.
3. **App** — Android with Media3 (ExoPlayer): catalog, detail and player. A **"Stats for nerds"** panel
   shows what the player is doing: the quality being downloaded vs. the one on screen, the bandwidth
   estimate and the buffer.

| Folder | What it is |
|---|---|
| [`pipeline/`](pipeline/) | FFmpeg: source video → HLS ladder + posters + catalog |
| [`server/`](server/) | Ktor: catalog API + HLS streams |
| [`app/`](app/) | Android: catalog → detail → player (MVI, feature × layer modules) |
| [`docs/`](docs/) | API contract and decisions |

## Content and licenses

All videos are either Creative Commons or public domain:
*Big Buck Bunny*, *Sintel*, *Tears of Steel* and *Elephants Dream* © Blender Foundation (CC BY), and
*Night of the Living Dead* (1968), *The General* (1926) and *The Immigrant* (1917) (public domain).
Full attribution is in [`pipeline/videos.json`](pipeline/videos.json) and inside the app.
