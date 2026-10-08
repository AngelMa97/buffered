package com.angelma.feature.catalog.presentation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.angelma.core.presentation.LocalNavAnimatedVisibilityScope
import com.angelma.feature.catalog.presentation.catalog.CatalogScreenRoot
import com.angelma.feature.catalog.presentation.detail.DetailScreenRoot
import kotlinx.serialization.Serializable


@Serializable data object CatalogGraph
@Serializable data object CatalogScreenRoute
@Serializable data class DetailScreenRoute(val videoId: String, val posterUrl: String)

fun NavGraphBuilder.catalogGraph(
    navController: NavController,
    onPlay: (streamUrl: String, title: String) -> Unit
) {
    navigation<CatalogGraph>(
        startDestination = CatalogScreenRoute,
    ) {
        composable<CatalogScreenRoute> {
            CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                CatalogScreenRoot(
                    onVideoSelected = { videoId, posterUrl ->
                        navController.navigate(DetailScreenRoute(videoId, posterUrl))
                    },
                )
            }
        }
        composable<DetailScreenRoute> {
            CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
                DetailScreenRoot(
                    onBack = { navController.navigateUp() },
                    onVideoPlay = { streamUrl, title ->
                        onPlay(streamUrl, title)
                    }
                )
            }
        }
    }
}