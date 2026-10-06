package com.angelma.feature.catalog.presentation.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.angelma.core.designsystem.BufferedTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DetailScreenRoot(
    onBack: () -> Unit,
    onVideoPlay: (streamUrl: String, title: String) -> Unit,
    videoId: String,
    viewModel: DetailViewModel = koinViewModel()
) {
    DetailScreen(
        onAction = { action ->
            when (action) {
                DetailAction.OnBackTap -> onBack()
                DetailAction.OnPlayVideo -> onVideoPlay("www.google.com","Google dot com")
            }
        }
    )
}

@Composable
fun DetailScreen(
    onAction: (DetailAction) -> Unit
) {
    Column {
        Text("DETAIL SCREEN")
        Button(onClick = { onAction(DetailAction.OnBackTap) }) { Text("Return to catalog") }
        Button(onClick = { onAction(DetailAction.OnPlayVideo) }) { Text("Play video") }
    }
}

@Preview
@Composable
private fun DetailScreenPreview() {
    BufferedTheme {
        DetailScreen(
            {}
        )
    }
}