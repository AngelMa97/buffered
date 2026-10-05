package com.angelma.bufferedclient

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.angelma.core.designsystem.BufferedTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))

        setContent {
            val context = LocalContext.current
            var granted by remember {
                mutableStateOf(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_LOCAL_NETWORK
                        ) == PERMISSION_GRANTED
                    } else true
                )
            }
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted -> granted = isGranted }

            LaunchedEffect(Unit) {
                if (!granted) {
                    launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                }
            }
            BufferedTheme {
                if (granted) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = {}) {
                            Text("Buffered working")
                        }
                    }
                } else {
                    AlertDialog(
                        onDismissRequest = { this@MainActivity.finish() },
                        title = { Text("Permission Required") },
                        text = { Text("Local networks are required to work properly") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    launcher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                                }
                            ) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    this@MainActivity.finish()
                                }
                            ) {
                                Text("Close app")
                            }
                        }
                    )
                }
            }
        }
    }
}