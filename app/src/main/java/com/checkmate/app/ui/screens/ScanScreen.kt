package com.checkmate.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.checkmate.app.data.scanner.ReceiptCameraPreview
import java.io.File

@Composable
fun ScanScreen(
    onClose: () -> Unit,
    onCaptured: (Uri) -> Unit,
    onManualEntry: () -> Unit,
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var camera by remember {
        mutableStateOf<Camera?>(null)
    }

    var imageCapture by remember {
        mutableStateOf<ImageCapture?>(null)
    }

    var flashOn by remember {
        mutableStateOf(false)
    }

    var isCapturing by remember {
        mutableStateOf(false)
    }

    var capturedUriString by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val capturedUri =
        capturedUriString?.let(Uri::parse)

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            hasCameraPermission = granted

            errorMessage = if (granted) {
                null
            } else {
                "Camera permission denied. You can import a receipt instead."
            }
        }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            if (uri != null) {
                errorMessage = null
                capturedUriString = uri.toString()
            }
        }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission && capturedUri == null) {
            permissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }

    fun resetCapture() {
        capturedUriString = null
        camera = null
        imageCapture = null
        flashOn = false
        errorMessage = null
    }

    fun captureReceipt() {

        val capture = imageCapture ?: return

        if (isCapturing) return

        isCapturing = true
        errorMessage = null

        val photoFile = try {
            File.createTempFile(
                "receipt_",
                ".jpg",
                context.cacheDir
            )
        } catch (exception: Exception) {
            isCapturing = false
            errorMessage = "Unable to create image file."
            return
        }

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(
                photoFile
            ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    output: ImageCapture.OutputFileResults
                ) {
                    isCapturing = false

                    camera?.cameraControl?.enableTorch(false)
                    flashOn = false

                    capturedUriString =
                        Uri.fromFile(photoFile).toString()
                }

                override fun onError(
                    exception: ImageCaptureException
                ) {
                    isCapturing = false
                    photoFile.delete()

                    errorMessage =
                        exception.message
                            ?: "Unable to capture receipt."
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1B3D))
    ) {

        // Real image or live camera preview
        when {
            capturedUri != null -> {

                AndroidView(
                    factory = { viewContext ->
                        ImageView(viewContext).apply {
                            scaleType =
                                ImageView.ScaleType.FIT_CENTER
                        }
                    },
                    update = { imageView ->
                        imageView.setImageURI(capturedUri)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 90.dp,
                            bottom = 170.dp
                        )
                )
            }

            hasCameraPermission -> {

                ReceiptCameraPreview(
                    modifier = Modifier.fillMaxSize(),

                    onCameraReady = { readyCamera, capture ->
                        camera = readyCamera
                        imageCapture = capture
                        errorMessage = null
                    },

                    onCameraError = { exception ->
                        camera = null
                        imageCapture = null

                        errorMessage =
                            exception.message
                                ?: "Camera unavailable."
                    }
                )

                // Visual alignment guide
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-24).dp)
                        .fillMaxWidth(0.76f)
                        .height(380.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFF4ADE80),
                            shape = RoundedCornerShape(16.dp)
                        )
                )
            }

            else -> {

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Camera access is required",
                        color = Color.White,
                        style =
                            MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Allow camera access or import a receipt from your gallery.",
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = {
                            permissionLauncher.launch(
                                Manifest.permission.CAMERA
                            )
                        }
                    ) {
                        Text("Allow camera")
                    }
                }
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onClose,
                enabled = !isCapturing
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close scanner",
                    tint = Color.White
                )
            }

            Text(
                text = if (capturedUri == null) {
                    "Scan receipt"
                } else {
                    "Review receipt"
                },
                modifier = Modifier.weight(1f),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.width(48.dp))
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xDD0F1B3D))
                .navigationBarsPadding()
                .padding(20.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            errorMessage?.let { message ->

                Text(
                    text = message,
                    color = Color(0xFFFCA5A5),
                    textAlign = TextAlign.Center,
                    style =
                        MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))
            }

            if (capturedUri != null) {

                Text(
                    text = "Make sure all receipt items are visible.",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        onCaptured(capturedUri)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Use receipt")
                }

                TextButton(
                    onClick = { resetCapture() }
                ) {
                    Text(
                        text = "Retake photo",
                        color = Color.White
                    )
                }

            } else {

                Text(
                    text = if (isCapturing) {
                        "Capturing receipt..."
                    } else {
                        "Position the receipt inside the frame"
                    },
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    // Torch
                    IconButton(
                        enabled =
                            camera?.cameraInfo?.hasFlashUnit() == true
                                    && !isCapturing,

                        onClick = {
                            flashOn = !flashOn

                            camera?.cameraControl
                                ?.enableTorch(flashOn)
                        }
                    ) {
                        Icon(
                            imageVector = if (flashOn) {
                                Icons.Filled.FlashOn
                            } else {
                                Icons.Filled.FlashOff
                            },
                            contentDescription = "Toggle flashlight",
                            tint = Color.White
                        )
                    }

                    // Shutter
                    FilledIconButton(
                        onClick = { captureReceipt() },

                        enabled =
                            imageCapture != null &&
                                    !isCapturing,

                        modifier = Modifier.size(76.dp),

                        shape = CircleShape,

                        colors = IconButtonDefaults
                            .filledIconButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF2563EB)
                            )
                    ) {

                        if (isCapturing) {

                            CircularProgressIndicator(
                                modifier = Modifier.size(30.dp),
                                strokeWidth = 3.dp
                            )

                        } else {

                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .border(
                                        3.dp,
                                        Color(0xFF2563EB),
                                        CircleShape
                                    )
                            )
                        }
                    }

                    // Gallery
                    IconButton(
                        enabled = !isCapturing,

                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts
                                        .PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Filled.PhotoLibrary,
                            contentDescription =
                                "Import from gallery",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                TextButton(
                    enabled = !isCapturing,
                    onClick = onManualEntry
                ) {
                    Text(
                        text = "Enter receipt manually",
                        color = Color.White
                    )
                }
            }
        }
    }
}
