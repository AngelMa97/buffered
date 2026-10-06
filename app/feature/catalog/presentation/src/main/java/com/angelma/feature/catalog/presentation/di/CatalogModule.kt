package com.angelma.feature.catalog.presentation.di

import com.angelma.feature.catalog.presentation.catalog.CatalogViewModel
import com.angelma.feature.catalog.presentation.detail.DetailViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val catalogModule = module {
    viewModelOf(::CatalogViewModel)
    viewModelOf(::DetailViewModel)
}