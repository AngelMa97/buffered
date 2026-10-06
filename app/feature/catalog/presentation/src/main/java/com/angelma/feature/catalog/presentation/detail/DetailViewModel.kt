package com.angelma.feature.catalog.presentation.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.angelma.core.domain.util.Result
import com.angelma.core.presentation.asUiText
import com.angelma.feature.catalog.domain.repository.CatalogRepository
import com.angelma.feature.catalog.presentation.DetailScreenRoute
import kotlinx.coroutines.launch

class DetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val videoId: String = savedStateHandle.toRoute<DetailScreenRoute>().videoId

    var state by mutableStateOf(DetailState())
        private set

    init {
        getVideoDetail()
    }

    fun onAction(action: DetailAction) {
        when(action) {
            DetailAction.OnRequestVideoDetail -> getVideoDetail()
            else -> Unit
        }
    }

    private fun getVideoDetail() {
        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            state = when(val response = catalogRepository.getVideo(videoId)) {
                is Result.Error -> state.copy(isLoading = false, error = response.error.asUiText())
                is Result.Success -> state.copy(isLoading = false, videoDetail = response.data)
            }
        }
    }
}