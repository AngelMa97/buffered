package com.angelma

import com.angelma.models.Video

interface CatalogRepository {

    fun getAllVideos(): List<Video>

    fun getVideoById(id: String): Video?
}