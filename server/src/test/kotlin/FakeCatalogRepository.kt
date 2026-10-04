package com.angelma

import com.angelma.models.License
import com.angelma.models.Rendition
import com.angelma.models.Video

class FakeCatalogRepository: CatalogRepository {
    private val videos = listOf(
        Video(
            id = "video-a",
            title = "Video A",
            year = 2020,
            description = "First fake video for tests.",
            durationSeconds = 120,
            master = "master.m3u8",
            poster = "poster.jpg",
            backdrop = "backdrop.jpg",
            license = License(
                name = "CC BY 3.0",
                url = "https://creativecommons.org/licenses/by/3.0/"
            ),
            attribution = "Test attribution A",
            renditions = listOf(
                Rendition(name = "1080p", width = 1920, height = 1080, bandwidth = 5_870_000),
                Rendition(name = "360p", width = 640, height = 360, bandwidth = 1_110_000),
            ),
        ),
        Video(
            id = "video-b",
            title = "Video B",
            year = 1926,
            description = "Second fake video for tests.",
            durationSeconds = 60,
            master = "master.m3u8",
            poster = "poster.jpg",
            backdrop = "backdrop.jpg",
            license = License(name = "Public Domain", url = "https://creativecommons.org/publicdomain/mark/1.0/"),
            attribution = "Test attribution B",
            renditions = listOf(
                Rendition(name = "480p", width = 640, height = 480, bandwidth = 1_550_000),
            ),
        ),
    )

    override fun getAllVideos(): List<Video> = videos

    override fun getVideoById(id: String): Video? = videos.find { x -> x.id == id }
}