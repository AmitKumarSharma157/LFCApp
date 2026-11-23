package com.example.lfcapp.domain.util

/**
 * Generic outcome wrapper for operations that can fail (e.g. network calls),
 * so the domain/presentation layers never need to catch exceptions or know
 * about HTTP status codes directly.
 */
sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Failure(val message: String, val cause: Throwable? = null) : DomainResult<Nothing>
}
