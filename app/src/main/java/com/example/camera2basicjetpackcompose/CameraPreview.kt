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

@Composable
fun CameraPreview() {
    val context = LocalContext.current // This is correct
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val previewView = remember { mutableStateOf<PreviewView?>(null) }
    val imageView =remember { mutableStateOf<ImageView?>(null) }
    var userTookPicture by remember { mutableStateOf(false) }

    Box {
        val imageCapture = ImageCapture.Builder().build()
        AndroidView(factory = { ctx ->
            FrameLayout(ctx).apply{
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Add PreviewView
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
                addView(previewView)

                // Add ImageView (initially hidden)
                val imageView = ImageView(ctx).apply {
                    visibility = View.GONE// Initially hide the ImageView
                    scaleType = ImageView.ScaleType.CENTER_CROP // Adjust scaling as needed
                }
                addView(imageView)
            }
        },
            update = { frameLayout ->
                previewView.value = frameLayout.getChildAt(0) as PreviewView
                imageView.value = frameLayout.getChildAt(1) as ImageView

                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.value?.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture)
                    } catch (e: Exception) {
                        // Handle exceptions
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )

        // Check if the user took a picture already, in which case we need to display a text.
        if (userTookPicture) {
            // Your composable for the buttons you want to add to the screen when the picture was taken.
            NewPictureButton {
                userTookPicture = false
                imageView.value?.visibility = View.GONE // Hide the ImageView
                previewView.value?.visibility = View.VISIBLE // Show the PreviewView
            }
        } else {
            // If the user didn't took a picture then they are in "taking a picture mode". We should render
            // three buttons.
            TakePictureButton(onTakePictureClicked = {
                // When clicking the "take picture" button, we should store a bitmap of the picture
                // that the user took. This keeps it in memory until the user decides to store it.
                takePhoto(
                    imageCapture = imageCapture,
                    executor = executor
                ) {
                    userTookPicture = true
                    val bitmap = it.toBitmap().rotate()
                    imageView.value?.setImageBitmap(bitmap)
                    imageView.value?.visibility = View.VISIBLE // Show the ImageView
                    previewView.value?.visibility = View.GONE // Hide the PreviewView
                }
            })
        }
    }
}

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
            // You can add further actions here, like displaying the image later
        }
    })
}

// Helper function to rotate a Bitmap
private fun Bitmap.rotate(): Bitmap {
    val matrix = Matrix().apply { postRotate(90f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
