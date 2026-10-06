package com.angelma.feature.catalog.presentation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.angelma.feature.catalog.presentation.catalog.CatalogScreenRoot
import com.angelma.feature.catalog.presentation.detail.DetailScreenRoot
import kotlinx.serialization.Serializable


@Serializable data object CatalogGraph
@Serializable data object CatalogScreenRoute
@Serializable data class DetailScreenRoute(val videoId: String)

fun NavGraphBuilder.catalogGraph(
    navController: NavController,
    onPlay: (streamUrl: String, title: String) -> Unit
) {
    navigation<CatalogGraph>(
        startDestination = CatalogScreenRoute,
    ) {
        composable<CatalogScreenRoute> {
            CatalogScreenRoot(
                onVideoSelected = { videoId ->
                    navController.navigate(DetailScreenRoute(videoId))
                },
            )
        }
        composable<DetailScreenRoute> {
            DetailScreenRoot(
                onBack = { navController.navigateUp() },
                onVideoPlay = { streamUrl, title ->
                    onPlay(streamUrl, title)
                }
            )
        }
    }
}