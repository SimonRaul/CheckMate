
package com.checkmate.app.data.scanner

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun ReceiptCameraPreview(
    modifier: Modifier = Modifier,
    onCameraReady: (Camera, ImageCapture) -> Unit,
    onCameraError: (Throwable) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnCameraReady =
        rememberUpdatedState(onCameraReady)

    val currentOnCameraError =
        rememberUpdatedState(onCameraError)

    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode =
                PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner, previewView) {

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        val executor =
            ContextCompat.getMainExecutor(context)

        var disposed = false

        var cameraProvider: ProcessCameraProvider? = null
        var boundPreview: Preview? = null
        var boundCapture: ImageCapture? = null

        cameraProviderFuture.addListener({

            if (!disposed) {
                try {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder()
                        .build()
                        .also {
                            it.setSurfaceProvider(
                                previewView.surfaceProvider
                            )
                        }

                    val imageCapture = ImageCapture.Builder()
                        .setCaptureMode(
                            ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                        )
                        .build()

                    val camera = provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageCapture
                    )

                    boundPreview = preview
                    boundCapture = imageCapture

                    currentOnCameraReady.value(
                        camera,
                        imageCapture
                    )

                } catch (exception: Exception) {
                    currentOnCameraError.value(exception)
                }
            }

        }, executor)

        onDispose {
            disposed = true

            val preview = boundPreview
            val capture = boundCapture

            if (preview != null && capture != null) {
                cameraProvider?.unbind(preview, capture)
            }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
