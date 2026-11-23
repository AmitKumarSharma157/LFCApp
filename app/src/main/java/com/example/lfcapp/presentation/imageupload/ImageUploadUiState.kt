package com.example.lfcapp.presentation.imageupload

/**
 * MVVM UI state for the image-upload screen. Presentation-only; never
 * leaks domain/data/network types into the composable layer.
 */
sealed interface ImageUploadUiState {
    data object Idle : ImageUploadUiState
    data object Loading : ImageUploadUiState
    data class Success(val status: String?, val parsedId: String?) : ImageUploadUiState
    data class Error(val message: String) : ImageUploadUiState
}
