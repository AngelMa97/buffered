package com.angelma.feature.catalog.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.testing.invoke
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.angelma.core.domain.util.DataError
import com.angelma.core.domain.util.Result
import com.angelma.feature.catalog.domain.models.License
import com.angelma.feature.catalog.domain.models.Rendition
import com.angelma.feature.catalog.domain.models.VideoDetail
import com.angelma.feature.catalog.presentation.DetailScreenRoute
import com.angelma.feature.catalog.presentation.FakeCatalogRepository
import com.angelma.feature.catalog.presentation.MainDispatcherRule
import com.angelma.feature.catalog.presentation.videoDetail
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCatalogRepository()

    @Test
    fun `read videoId and posterUrl from SavedStateHandle then download video detail`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val savedStateHandle = SavedStateHandle.Companion(
                route = DetailScreenRoute(videoId = "sintel", posterUrl = "http://test/poster.jpg")
            )
            val videoDetail = videoDetail()
            repository.videoResult = Result.Success(videoDetail)
            val viewModel = DetailViewModel(savedStateHandle, repository)

            assertThat(viewModel.state.isLoading).isTrue()
            assertThat(viewModel.state.videoId).isEqualTo("sintel")
            assertThat(viewModel.state.posterUrl).isEqualTo("http://test/poster.jpg")
            assertThat(viewModel.state.isLoading).isTrue()

            advanceUntilIdle()

            assertThat(viewModel.state.isLoading).isFalse()
            assertThat(viewModel.state.error).isNull()
            assertThat(viewModel.state.videoDetail?.id).isEqualTo("sintel")
        }

    @Test
    fun `download video detail fails`() = runTest(mainDispatcherRule.testDispatcher) {
        val savedStateHandle = SavedStateHandle.Companion(
            route = DetailScreenRoute(videoId = "sintel", posterUrl = "http://test/poster.jpg")
        )
        repository.videoResult = Result.Error(DataError.Network.SERVER_ERROR)

        val viewModel = DetailViewModel(savedStateHandle, repository)

        assertThat(viewModel.state.isLoading).isTrue()

        advanceUntilIdle()

        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.error).isNotNull()
        assertThat(viewModel.state.videoDetail).isNull()
    }

    @Test
    fun `retry get video detail after error`() = runTest(mainDispatcherRule.testDispatcher) {
        repository.videoResult = Result.Error(DataError.Network.SERVER_ERROR)
        val savedStateHandle = SavedStateHandle.Companion(
            route = DetailScreenRoute(videoId = "sintel", posterUrl = "http://test/poster.jpg")
        )

        val viewModel = DetailViewModel(savedStateHandle, repository)

        assertThat(viewModel.state.videoId).isEqualTo("sintel")
        assertThat(viewModel.state.posterUrl).isEqualTo("http://test/poster.jpg")
        assertThat(viewModel.state.isLoading).isTrue()

        advanceUntilIdle()

        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.error).isNotNull()
        assertThat(viewModel.state.videoDetail).isNull()

        val videoDetail = videoDetail()

        repository.videoResult = Result.Success(videoDetail)
        viewModel.onAction(DetailAction.OnRequestVideoDetail)

        assertThat(viewModel.state.error).isNull()
        assertThat(viewModel.state.isLoading).isTrue()

        advanceUntilIdle()

        assertThat(viewModel.state.isLoading).isFalse()
        assertThat(viewModel.state.error).isNull()
        assertThat(viewModel.state.videoDetail).isNotNull()
    }
}