package com.example.camera2basicjetpackcompose

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

// CameraPreview is a composable node that will display the camera of the device in full screen mode.
// This is contained in a Box composable with an AndroidView composable. This composable assumes
// the camera permission has already been granted.
@Composable
fun CameraPreview() {
    // Get the local context.
    val context = LocalContext.current
    // Get the LifecycleOwner.
    val lifecycleOwner = LocalLifecycleOwner.current
    // Create a mutable state to store the Preview.
    var preview by remember { mutableStateOf<Preview?>(null) }
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val previewView = remember { mutableStateOf<PreviewView?>(null) }
    val imageView =remember { mutableStateOf<ImageView?>(null) }
    var userTookPicture by remember { mutableStateOf(false) }

    // Make a Box and expand it to all the screen.
    Box {
        val imageCapture = ImageCapture.Builder().build()
        // Use an AndroidView to display the camera preview.
        AndroidView(factory = { ctx ->
            // We're going to use a FrameLayout to switch between displaying the camera and showing
            // a static picture.
            FrameLayout(ctx).apply{
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Add PreviewView, this will show the camera.
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                addView(previewView)

                // Add ImageView (initially hidden), this will display a static picture.
                val imageView = ImageView(ctx).apply {
                    visibility = View.GONE// Initially hide the ImageView
                    scaleType = ImageView.ScaleType.CENTER_CROP // Adjust scaling as needed
                }
                addView(imageView)
            }
        },
            // Update hte frame layout with the PreviewView and ImageView.
            update = { frameLayout ->
                previewView.value = frameLayout.getChildAt(0) as PreviewView
                imageView.value = frameLayout.getChildAt(1) as ImageView

                // Get an instance of the CameraProvider asynchronously.
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                // Add a listener to be notified when the CameraProvider is available.
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    // Build a Preview use case and set the surface provider from the PreviewView.
                    preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.value?.surfaceProvider)
                    }
                    // Select the default back camera. You can change this to the font camera if
                    // needed.
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        // Unbind any previously bound use cases.
                        cameraProvider.unbindAll()
                        // Bind the Preview and ImageCapture use cases to the camera lifecycle.
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture)
                    } catch (e: Exception) {
                        // Handle exceptions
                    }
                }, ContextCompat.getMainExecutor(context)) // Run the listener on the main executor.
            }
        )

        // At this point the screen has the camera showing up to the full screen. We're now going to
        // add some buttons overlaying the camera preview.

        // Check if the user took a picture already, in which case we need to display a Refresh button..
        if (userTookPicture) {
            // Your composable for the buttons you want to add to the screen when the picture was taken.
            NewPictureButton {
                userTookPicture = false
                imageView.value?.visibility = View.GONE // Hide the ImageView
                previewView.value?.visibility = View.VISIBLE // Show the PreviewView
            }
        } else {
            // If the user didn't took a picture then they are in "taking a picture mode".
            TakePictureButton(onTakePictureClicked = {
                // When clicking the "take picture" button, we should store a bitmap of the picture
                // that the user took. This bitmap will be used to display it as a static picture.
                takePhoto(
                    imageCapture = imageCapture,
                    executor = executor
                ) {
                    userTookPicture = true
                    // For some reason, we need to rotate this 90 degrees. Idk why.
                    val bitmap = it.toBitmap().rotate()
                    imageView.value?.setImageBitmap(bitmap)

                    imageView.value?.visibility = View.VISIBLE // Show the ImageView
                    previewView.value?.visibility = View.GONE // Hide the PreviewView
                }
            })
        }
    }
}

// Function to take a picture.
private fun takePhoto(
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (ImageProxy) -> Unit
) {
    Log.d("CameraPreview", "Taking photo...")
    // Actually take the picture.
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onError(exc: ImageCaptureException) {
            Log.e("TakePhoto", "Photo capture failed: ${exc.message}", exc)
        }

        override fun onCaptureSuccess(image: ImageProxy) {
            Log.d("TakePhoto", "Photo capture succeeded")
            onImageCaptured(image)
            image.close() // Important: Close the ImageProxy to release resources
            // You can add further actions here.
        }
    })
}

// Helper function to rotate a Bitmap.
private fun Bitmap.rotate(): Bitmap {
    // Rotate it 90 degrees.
    val matrix = Matrix().apply { postRotate(90f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
