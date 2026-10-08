package com.angelma.feature.catalog.presentation

import com.angelma.core.domain.util.DataError
import com.angelma.core.domain.util.Result
import com.angelma.feature.catalog.domain.models.License
import com.angelma.feature.catalog.domain.models.Rendition
import com.angelma.feature.catalog.domain.models.VideoDetail
import com.angelma.feature.catalog.domain.models.VideoSummary
import com.angelma.feature.catalog.domain.repository.CatalogRepository

class FakeCatalogRepository : CatalogRepository {

    var videosResult: Result<List<VideoSummary>, DataError.Network> = Result.Success(emptyList())
    var videoResult: Result<VideoDetail, DataError.Network> = Result.Error(DataError.Network.NOT_FOUND)

    var getVideosCalls = 0
        private set

    override suspend fun getVideos(): Result<List<VideoSummary>, DataError.Network> {
        getVideosCalls++
        return videosResult
    }

    override suspend fun getVideo(id: String): Result<VideoDetail, DataError.Network> = videoResult
}

fun videoSummary(id: String) = VideoSummary(
    id = id,
    title = "Title $id",
    year = 2010,
    durationSeconds = 60,
    posterUrl = "http://test/media/$id/poster.jpg",
)

fun videoDetail() = VideoDetail(
    id = "sintel",
    title = "Sintel",
    year = 0,
    description = "",
    durationSeconds = 0,
    posterUrl = "http://test/poster.jpg",
    backdropUrl = "",
    streamUrl = "",
    license = License(
        name = "",
        url = ""
    ),
    attribution = "",
    renditions = listOf(
        Rendition(
            name = "",
            width = 1,
            height = 2,
            bandwidth = 1
        )
    )
)
