package com.example.camera2basicjetpackcompose

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// CameraScreen makes sure we have all the permissions necessary in order to run the CameraPreview
// composable. This will essentially pop up a message to the user asking for permission to use
// the camera.
@Composable
fun CameraScreen() {
    // A variable to check if the permission is granted or not.
    var hasCameraPermission by remember { mutableStateOf(false) }

    // Create a launcher that will request the permission to the user.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Save the state of the permission on our variable.
        hasCameraPermission = isGranted
    }

    // Actually request the permission.
    LaunchedEffect(key1 = true) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    // If the user said "yes" then we can safely display the camera preview.
    if (hasCameraPermission) {
        Log.i("CameraScreen", "Camera permission granted")
        CameraPreview()
    } else {
        // If the user said "no" then we show an error message. Alternatively we can display
        // a warning message to the user saying the app will not work as expected, and direct
        // the user to Settings in order to manually grant this app the Camera permission.
        Log.e("CameraScreen", "Camera permission denied")
    }
}