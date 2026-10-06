package com.angelma.feature.player.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.angelma.core.designsystem.BufferedTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayerScreenRoot(
    onBack: () -> Unit,
    stream: String,
    title: String,
    viewModel: PlayerViewModel = koinViewModel()
) {
    PlayerScreen(
        onAction = { action ->
            when(action) {
                PlayerAction.OnBackTap -> onBack()
            }
        }
    )
}

@Composable
fun PlayerScreen(
    onAction: (PlayerAction) -> Unit
) {
    Column {
        Text("PLAYER SCREEN")
        Button(onClick = { onAction(PlayerAction.OnBackTap) } ) { Text("Back to detail") }
    }
}

@Preview
@Composable
private fun PlayerScreenPreview() {
    BufferedTheme {
        PlayerScreen(
            {}
        )
    }
}