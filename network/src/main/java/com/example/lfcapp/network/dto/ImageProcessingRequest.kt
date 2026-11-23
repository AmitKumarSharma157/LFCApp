package com.example.lfcapp.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Wire-format request body for POST ImageProcessing/ProcessImage.
 * Field names use @Json(name=...) to decouple the Kotlin property name from
 * the exact JSON key expected by the backend.
 */
@JsonClass(generateAdapter = true)
data class ImageProcessingRequest(
    @Json(name = "Image") val Image: String,
    @Json(name = "UnitId") val UnitId: String,
    @Json(name = "PrinterName") val PrinterName: String,
    @Json(name = "UserData") val UserData: String,
    @Json(name = "MessageId") val MessageId: String,
)
