package com.angelma

import com.angelma.mappers.toVideo
import com.angelma.models.Video
import com.angelma.records.Catalog
import kotlinx.serialization.json.Json
import java.io.File

class FileCatalogRepository(
    mediaDir: File
): CatalogRepository {

    private val catalog: Catalog

    init {
        val catalogFile = File(mediaDir, "catalog.json")
        val mJson = Json { ignoreUnknownKeys = true }
        catalog = mJson.decodeFromString<Catalog>(catalogFile.readText())
    }

    override fun getAllVideos(): List<Video> {
        return catalog.videos.map { videoRecord -> videoRecord.toVideo() }
    }

    override fun getVideoById(id: String): Video? {
        return catalog.videos.find { x -> x.id == id }?.toVideo()
    }
}