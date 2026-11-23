package com.example.lfcapp.domain.model

/**
 * Domain-layer result of an image-processing request.
 */
data class ProcessedImage(
    val Status: String,
    val ParsedID: String,
)
