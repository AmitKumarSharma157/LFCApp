package com.example.lfcapp.network.handler

/**
 * Network-layer outcome of a single API call. Distinct from the domain
 * layer's `DomainResult` — this type still carries HTTP-specific details
 * (status code, raw error body) so the data layer can decide how to map
 * them into a domain-level failure.
 */
sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>

    /** The server responded, but with a non-2xx status code. */
    data class ApiError(val code: Int, val errorBody: String?) : NetworkResult<Nothing>

    /** The request never reached/returned from the server (timeout, no connectivity, etc). */
    data class NetworkError(val message: String, val cause: Throwable) : NetworkResult<Nothing>

    /** Any other unexpected failure (serialization bug, programming error, etc). */
    data class UnknownError(val message: String, val cause: Throwable) : NetworkResult<Nothing>
}
