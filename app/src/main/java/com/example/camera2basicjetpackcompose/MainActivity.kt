package com.example.camera2basicjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.camera2basicjetpackcompose.ui.theme.Camera2BasicJetpackComposeTheme

// Main entrypoint for the app.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Camera2BasicJetpackComposeTheme {
                // Just for display we show the camera composable. In a real implementation you should have a
                // NavHost controller to route the user to the camera.
                CameraScreen()
            }
        }
    }
}
