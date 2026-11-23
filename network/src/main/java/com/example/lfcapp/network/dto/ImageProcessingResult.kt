package com.example.lfcapp.network.dto

/**
 * Wire-format response body for POST ImageProcessing/ProcessImage.
 * Both fields are nullable/defaulted because the backend may omit them on
 * certain status outcomes (e.g. failure responses without a parsedID).
 * Deserialized by [ImageProcessingResultAdapter] (case-insensitive keys).
 */
data class ImageProcessingResult(
    val Status: String? = null,
    val ParsedID: String? = null,
)
