package com.example.camera2basicjetpackcompose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import android.util.Log
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh

@Composable
fun NewPictureButton(onNewPictureButtonClick: () -> Unit) {
    Box (modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .size(80.dp) // Adjust outer size as needed
                    .border(4.dp, Color.White, CircleShape) // White border (hollow circle)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp) // Adjust inner size as needed
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .clickable {
                            Log.i("NewPictureButton", "New picture button clicked")
                            onNewPictureButtonClick()
                        }
                ) {
                    Icon(
                        // Use a replay icon.
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Replay",
                        tint = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}