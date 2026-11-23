package com.example.lfcapp.domain.model

/**
 * Domain-layer input for uploading/processing an image. Free of any
 * DTO/JSON/Retrofit concerns.
 *
 * @property image Base64-encoded JPEG/PNG bytes (e.g. captured via CameraX).
 */
data class ImageUploadRequest(
    val Image: String,
    val UnitId: String,
    val PrinterName: String,
    val UserData: String,
    val MessageId: String,
)
