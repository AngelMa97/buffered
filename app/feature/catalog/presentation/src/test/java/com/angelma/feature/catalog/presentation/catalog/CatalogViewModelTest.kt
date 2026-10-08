package com.angelma.feature.catalog.presentation.catalog

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.angelma.core.domain.util.DataError
import com.angelma.core.domain.util.Result
import com.angelma.feature.catalog.presentation.FakeCatalogRepository
import com.angelma.feature.catalog.presentation.MainDispatcherRule
import com.angelma.feature.catalog.presentation.videoSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCatalogRepository()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loads the catalog on start`() = runTest(mainDispatcherRule.testDispatcher) {
        val videos = listOf(videoSummary("sintel"), videoSummary("big-buck-bunny"))
        repository.videosResult = Result.Success(videos)

        val viewModel = CatalogViewModel(repository)

        assertThat(viewModel.state.isLoading).isTrue()

        advanceUntilIdle()

        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.error).isNull()
        assertThat(viewModel.state.videos).containsExactly(*videos.toTypedArray())
    }

    @Test
    fun `load catalog fails`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.videosResult = Result.Error(DataError.Network.NO_INTERNET)

        val viewModel = CatalogViewModel(repository)

        assertThat(viewModel.state.isLoading).isTrue()

        advanceUntilIdle()

        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.error).isNotNull()
        assertThat(viewModel.state.videos).isEmpty()
    }

    @Test
    fun `retry get catalog after error`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.videosResult = Result.Error(DataError.Network.NO_INTERNET)

        val viewModel = CatalogViewModel(repository)

        advanceUntilIdle()

        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.error).isNotNull()
        assertThat(viewModel.state.videos).isEmpty()

        val videos = listOf(videoSummary("sintel"), videoSummary("big-buck-bunny"))
        repository.videosResult = Result.Success(videos)
        viewModel.onAction(CatalogAction.OnRequestVideos)

        assertThat(viewModel.state.error).isNull()
        assertThat(viewModel.state.isLoading).isTrue()

        advanceUntilIdle()

        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.error).isNull()
        assertThat(viewModel.state.videos.size).isEqualTo(2)

    }

}
