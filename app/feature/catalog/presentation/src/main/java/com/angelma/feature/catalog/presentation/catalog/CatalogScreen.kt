package com.angelma.feature.catalog.presentation.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.angelma.core.designsystem.BufferedTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun CatalogScreenRoot(
    onVideoSelected: (videoId: String) -> Unit,
    viewModel: CatalogViewModel = koinViewModel()
) {
    CatalogScreen(
        onAction = { action ->
            when(action) {
                is CatalogAction.OnVideoSelect -> onVideoSelected(action.videoId)
                else -> {}
            }
        }
    )
}

@Composable
fun CatalogScreen(
    onAction: (CatalogAction) -> Unit
) {
    Column {
        Text("CATALOG SCREEN")
        Button(onClick = { onAction(CatalogAction.OnVideoSelect("some-video")) }) {
            Text("Go to video detail")
        }
    }
}

@Preview
@Composable
private fun CatalogScreenPreview() {
    BufferedTheme {
        CatalogScreen(
            {}
        )
    }
}