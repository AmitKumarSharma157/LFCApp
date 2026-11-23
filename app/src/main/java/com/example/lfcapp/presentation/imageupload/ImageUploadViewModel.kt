package com.example.lfcapp.presentation.imageupload

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lfcapp.camera.ImageEncoder
import com.example.lfcapp.domain.model.ImageUploadRequest
import com.example.lfcapp.domain.usecase.ProcessImageUseCase
import com.example.lfcapp.domain.util.DomainResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ImageUploadViewModel @Inject constructor(
    private val processImage: ProcessImageUseCase,
    private val imageEncoder: ImageEncoder,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImageUploadUiState>(ImageUploadUiState.Idle)
    val uiState: StateFlow<ImageUploadUiState> = _uiState.asStateFlow()

    // Kept here (not in the screen) so the photo survives rotation while the camera is closed.
    private val _capturedPhoto = MutableStateFlow<Bitmap?>(null)
    val capturedPhoto: StateFlow<Bitmap?> = _capturedPhoto.asStateFlow()

    /**
     * Must be called from a background thread (the camera callback executor). Runs synchronously so
     * the photo is ready before the screen closes the camera.
     */
    @WorkerThread
    fun onPhotoCaptured(frame: Bitmap, rotationDegrees: Int) {
        try {
            _capturedPhoto.value = imageEncoder.prepare(frame, rotationDegrees)
            _uiState.value = ImageUploadUiState.Idle
        } catch (e: Exception) {
            _uiState.value = ImageUploadUiState.Error("Could not process the photo: ${e.message}")
        }
    }

    fun retake() {
        _capturedPhoto.value = null
        _uiState.value = ImageUploadUiState.Idle
    }

    /** Encodes the captured photo off the main thread, then uploads it. */
    fun uploadCapturedPhoto(unitId: String, printerName: String, userData: String) {
        val photo = _capturedPhoto.value ?: return
        _uiState.value = ImageUploadUiState.Loading
        viewModelScope.launch {
            val imageBase64 = try {
                withContext(Dispatchers.Default) { imageEncoder.toBase64Jpeg(photo) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = ImageUploadUiState.Error("Could not process the photo: ${e.message}")
                return@launch
            }

            upload(
                ImageUploadRequest(
                    Image = imageBase64,
                    UnitId = unitId,
                    PrinterName = printerName,
                    UserData = userData,
                    MessageId = UUID.randomUUID().toString(),
                )
            )
        }
    }

    fun onCaptureFailed(message: String) {
        _uiState.value = ImageUploadUiState.Error("Could not take the photo: $message")
    }

    private suspend fun upload(request: ImageUploadRequest) {
        _uiState.value = when (val result = processImage(request)) {
            is DomainResult.Success -> ImageUploadUiState.Success(
                status = result.data.Status,
                parsedId = result.data.ParsedID,
            )
            is DomainResult.Failure -> ImageUploadUiState.Error(result.message)
        }
    }
}
