package com.angelma.feature.player.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable data class PlayerGraph(val streamUrl: String, val title: String)
@Serializable data object PlayerScreenRoute

fun NavGraphBuilder.playerGraph(navController: NavController) {
    navigation<PlayerGraph>(
        startDestination = PlayerScreenRoute,
        enterTransition = { fadeIn(tween(300)) },
        popExitTransition = { fadeOut(tween(300)) },
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