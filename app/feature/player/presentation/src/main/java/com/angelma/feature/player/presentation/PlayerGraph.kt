package com.angelma.feature.player.presentation

import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data class PlayerGraph(val streamUrl: String, val title: String)
@Serializable data object PlayerScreenRoute

fun NavGraphBuilder.playerGraph(navController: NavController) {
    navigation<PlayerGraph>(
        startDestination = PlayerScreenRoute,
    ) {
        composable<PlayerScreenRoute> { backstackEntry ->
            val entry = remember(backstackEntry) {
                navController.getBackStackEntry<PlayerGraph>()
            }

            val args = entry.toRoute<PlayerGraph>()
            PlayerScreenRoot(
                onBack = { navController.navigateUp() }
            )
        }
    }
}