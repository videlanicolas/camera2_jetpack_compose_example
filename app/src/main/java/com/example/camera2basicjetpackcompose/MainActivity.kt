package com.example.camera2basicjetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.camera2basicjetpackcompose.ui.theme.Camera2BasicJetpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Camera2BasicJetpackComposeTheme {
                CameraScreen()
            }
        }
    }
}
