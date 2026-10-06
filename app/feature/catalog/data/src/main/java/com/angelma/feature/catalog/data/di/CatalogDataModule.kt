package com.angelma.feature.catalog.data.di

import com.angelma.feature.catalog.data.repository.KtorCatalogRepository
import com.angelma.feature.catalog.domain.repository.CatalogRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val catalogDataModule = module {
    singleOf(::KtorCatalogRepository).bind<CatalogRepository>()
}