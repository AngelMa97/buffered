package com.angelma.feature.catalog.presentation.catalog

import androidx.lifecycle.ViewModel
import com.angelma.feature.catalog.domain.repository.CatalogRepository

class CatalogViewModel(
    private val catalogRepository: CatalogRepository
) : ViewModel() {

}