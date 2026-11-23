package com.example.lfcapp.domain.repository

import com.example.lfcapp.domain.model.ImageUploadRequest
import com.example.lfcapp.domain.model.ProcessedImage
import com.example.lfcapp.domain.util.DomainResult

/**
 * Domain-layer contract implemented by the data layer. The domain module
 * knows nothing about Retrofit/HTTP; it only deals with domain models and
 * [DomainResult].
 */
interface ImageProcessingRepository {
    suspend fun processImage(request: ImageUploadRequest): DomainResult<ProcessedImage>
}
