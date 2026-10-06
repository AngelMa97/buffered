package com.angelma.feature.catalog.data.repository

import com.angelma.core.data.networking.get
import com.angelma.core.domain.util.DataError
import com.angelma.core.domain.util.Result
import com.angelma.core.domain.util.map
import com.angelma.feature.catalog.data.dtos.VideoDetailDTO
import com.angelma.feature.catalog.data.dtos.VideoSummaryDTO
import com.angelma.feature.catalog.data.dtos.toVideoDetail
import com.angelma.feature.catalog.data.dtos.toVideoSummary
import com.angelma.feature.catalog.domain.models.VideoDetail
import com.angelma.feature.catalog.domain.models.VideoSummary
import com.angelma.feature.catalog.domain.repository.CatalogRepository
import io.ktor.client.HttpClient

class KtorCatalogRepository(
    private val httpClient: HttpClient,
): CatalogRepository {

    override suspend fun getVideos(): Result<List<VideoSummary>, DataError.Network> = httpClient
        .get<List<VideoSummaryDTO>>(
            route = "/api/videos"
        ).map { videosDto ->
            videosDto.map { it.toVideoSummary() }
        }


    override suspend fun getVideo(id: String): Result<VideoDetail, DataError.Network> = httpClient
        .get<VideoDetailDTO>(
            route = "/api/videos/$id"
        ).map { it.toVideoDetail() }
}