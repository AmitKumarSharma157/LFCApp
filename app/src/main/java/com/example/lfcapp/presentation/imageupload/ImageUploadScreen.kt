package com.example.lfcapp.presentation.imageupload

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lfcapp.camera.CameraPreview
import com.example.lfcapp.ui.theme.LFCAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

@Composable
fun ImageUploadScreen(
    modifier: Modifier = Modifier,
    viewModel: ImageUploadViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val capturedPhoto by viewModel.capturedPhoto.collectAsStateWithLifecycle()

    // The camera is only opened on demand, so it isn't streaming while the user fills in the form.
    var isCameraOpen by rememberSaveable { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(context.hasCameraPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionDenied = !granted
        isCameraOpen = granted
    }
    // Picks up a permission granted or revoked from system Settings while the app was in the background.
    LifecycleResumeEffect(Unit) {
        hasCameraPermission = context.hasCameraPermission()
        if (hasCameraPermission) permissionDenied = false else isCameraOpen = false
        onPauseOrDispose { }
    }

    fun openCamera() {
        if (hasCameraPermission) isCameraOpen = true
        else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        }
    }
    DisposableEffect(lifecycleOwner, hasCameraPermission, isCameraOpen) {
        if (hasCameraPermission && isCameraOpen) cameraController.bindToLifecycle(lifecycleOwner)
        onDispose { cameraController.unbind() }
    }

    fun takePhoto() {
        isCapturing = true
        val mainExecutor = ContextCompat.getMainExecutor(context)
        cameraController.takePicture(
            // Decoding the captured JPEG into a Bitmap is too slow for the main thread.
            Dispatchers.Default.asExecutor(),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    image.use { viewModel.onPhotoCaptured(it.toBitmap(), it.imageInfo.rotationDegrees) }
                    mainExecutor.execute {
                        isCapturing = false
                        isCameraOpen = false
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    viewModel.onCaptureFailed(exception.message ?: "unknown error")
                    mainExecutor.execute { isCapturing = false }
                }
            },
        )
    }

    val isUploading = uiState is ImageUploadUiState.Loading

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val photoArea = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
        val photo = capturedPhoto
        when {
            isCameraOpen && hasCameraPermission -> {
                CameraPreview(controller = cameraController, modifier = photoArea)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Left enabled while capturing: closing the camera fails a stuck capture via onError.
                    OutlinedButton(
                        onClick = { isCameraOpen = false },
                        modifier = Modifier.weight(1f),
                    ) { Text("Cancel") }
                    Button(
                        onClick = { takePhoto() },
                        enabled = !isCapturing,
                        modifier = Modifier.weight(1f),
                    ) { Text(if (isCapturing) "Capturing…" else "Capture") }
                }
            }

            photo != null -> {
                val imageBitmap = remember(photo) { photo.asImageBitmap() }
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Captured photo",
                    contentScale = ContentScale.Crop,
                    modifier = photoArea,
                )
                OutlinedButton(
                    onClick = {
                        viewModel.retake()
                        openCamera()
                    },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Retake") }
            }

            else -> NoPhotoPlaceholder(
                permissionDenied = permissionDenied,
                onTakePhoto = ::openCamera,
                modifier = photoArea,
            )
        }

        ImageUploadStatus(uiState)
    }
}

@Composable
private fun NoPhotoPlaceholder(
    permissionDenied: Boolean,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (permissionDenied) {
                Text(
                    text = "Camera access is needed to photograph the label. " +
                        "If the request no longer appears, enable it in system Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(onClick = onTakePhoto) { Text("Take photo") }
        }
    }
}

@Composable
fun ImageUploadStatus(uiState: ImageUploadUiState, modifier: Modifier = Modifier) {
    val text = when (uiState) {
        ImageUploadUiState.Idle -> "Take a photo and fill in the details, then upload."
        ImageUploadUiState.Loading -> "Uploading…"
        is ImageUploadUiState.Success -> "Status: ${uiState.status}, Parsed ID: ${uiState.parsedId}"
        is ImageUploadUiState.Error -> "Error: ${uiState.message}"
    }
    val color = if (uiState is ImageUploadUiState.Error) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Text(text = text, color = color, modifier = modifier)
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED

@Preview(showBackground = true)
@Composable
private fun ImageUploadStatusPreview() {
    LFCAppTheme {
        ImageUploadStatus(ImageUploadUiState.Success(status = "OK", parsedId = "12345"))
    }
}
