package com.angelma.feature.player.presentation.di

import com.angelma.feature.player.presentation.PlayerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val playerModule = module {
    viewModelOf(::PlayerViewModel)
}