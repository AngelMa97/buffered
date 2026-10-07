package com.angelma.bufferedclient

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.angelma.feature.catalog.presentation.CatalogGraph
import com.angelma.feature.catalog.presentation.catalogGraph
import com.angelma.feature.player.presentation.PlayerGraph
import com.angelma.feature.player.presentation.playerGraph

@Composable
fun NavigationRoot(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = CatalogGraph,
        enterTransition = { fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 8 } },
        exitTransition = { fadeOut(tween(300)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(300)) + slideOutHorizontally(tween(300)) { it / 8 } },
    ) {
        catalogGraph(navController) { streamUrl, title ->
            navController.navigate(PlayerGraph(streamUrl, title))
        }
        playerGraph(navController)
    }
}
