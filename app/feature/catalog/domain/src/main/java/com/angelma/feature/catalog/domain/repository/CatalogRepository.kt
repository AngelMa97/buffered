package com.angelma.feature.catalog.domain.repository

import com.angelma.core.domain.util.DataError
import com.angelma.core.domain.util.Result
import com.angelma.feature.catalog.domain.models.VideoDetail
import com.angelma.feature.catalog.domain.models.VideoSummary

interface CatalogRepository {
    suspend fun getVideos(): Result<List<VideoSummary>, DataError.Network>
    suspend fun getVideo(id: String): Result<VideoDetail, DataError.Network>
}