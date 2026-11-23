package com.example.lfcapp.network.api

import com.example.lfcapp.network.dto.ImageProcessingRequest
import com.example.lfcapp.network.dto.ImageProcessingResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Single Retrofit contract for every remote endpoint used by the app.
 * Consolidating all endpoints here (instead of one interface per feature)
 * means there is exactly one Retrofit proxy instance and one place to look
 * when auditing the app's full network surface.
 *
 * Each suspend function returns a raw Retrofit [Response] so [safeApiCall]
 * (see NetworkCallHandler.kt) can uniformly translate success/HTTP-error/
 * exception into a [com.example.lfcapp.network.handler.NetworkResult].
 */
interface ApiService {

    @Headers("Accept: application/json")
    @POST("ImageProcessing/ProcessImage")
    suspend fun processImage(
        @Body request: ImageProcessingRequest,
    ): Response<ImageProcessingResult>
}
