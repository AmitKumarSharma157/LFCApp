package com.example.lfcapp.network.handler

import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs a Retrofit call and converts the outcome into a [NetworkResult], so
 * repositories don't repeat try/catch and `isSuccessful` checks:
 *
 * ```
 * safeApiCall { apiService.processImage(request) }
 * ```
 */
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> = try {
    val response = call()
    val body = response.body()
    when {
        response.isSuccessful && body != null -> NetworkResult.Success(body)
        response.isSuccessful -> NetworkResult.ApiError(response.code(), "Empty response body")
        else -> NetworkResult.ApiError(response.code(), response.errorBody()?.string())
    }
} catch (e: CancellationException) {
    throw e
} catch (e: IOException) {
    NetworkResult.NetworkError("Network error: ${e.message}", e)
} catch (e: Exception) {
    NetworkResult.UnknownError("Unexpected error: ${e.message}", e)
}
