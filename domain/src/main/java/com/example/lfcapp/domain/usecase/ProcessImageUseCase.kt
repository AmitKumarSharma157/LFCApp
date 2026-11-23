package com.example.lfcapp.domain.usecase

import com.example.lfcapp.domain.model.ImageUploadRequest
import com.example.lfcapp.domain.model.ProcessedImage
import com.example.lfcapp.domain.repository.ImageProcessingRepository
import com.example.lfcapp.domain.util.DomainResult
import javax.inject.Inject

/**
 * Single-responsibility use case invoked by the presentation layer to submit
 * an image for processing.
 */
class ProcessImageUseCase @Inject constructor(
    private val repository: ImageProcessingRepository
) {
    suspend operator fun invoke(request: ImageUploadRequest): DomainResult<ProcessedImage> =
        repository.processImage(request)
}
