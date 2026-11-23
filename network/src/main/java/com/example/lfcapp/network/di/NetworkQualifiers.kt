package com.example.lfcapp.network.di

import javax.inject.Qualifier

/**
 * Marks the base URL string that must be provided by a consumer of this
 * module (typically :app, since only it knows the environment-specific
 * BuildConfig value). This keeps :network reusable and environment-agnostic.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl
