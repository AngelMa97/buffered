package com.angelma.feature.catalog.presentation.catalog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angelma.core.domain.util.Result
import com.angelma.core.presentation.asUiText
import com.angelma.feature.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.launch

class CatalogViewModel(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    var state by mutableStateOf(CatalogState())
        private set

    init {
        getVideos()
    }

    fun onAction(action: CatalogAction) {
        when (action) {
            CatalogAction.OnRequestVideos -> getVideos()
            else -> Unit
        }
    }

    private fun getVideos() {
        state = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            val result = catalogRepository.getVideos()
            state = when (result) {
                is Result.Error -> state.copy(
                    error = result.error.asUiText(),
                    isLoading = false
                )

                is Result.Success -> state.copy(
                    videos = result.data,
                    isLoading = false
                )
            }
        }
    }
}