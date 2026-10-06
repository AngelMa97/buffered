package com.angelma.bufferedclient

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
        startDestination = CatalogGraph
    ) {
        catalogGraph(navController) { streamUrl, title ->
            navController.navigate(PlayerGraph(streamUrl, title))
        }
        playerGraph(navController)
    }
}
