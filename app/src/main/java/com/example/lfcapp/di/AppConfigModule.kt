package com.example.lfcapp.di

import com.example.lfcapp.BuildConfig
import com.example.lfcapp.network.di.BaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Supplies environment-specific configuration to :network. Kept in :app
 * because BuildConfig fields are generated per-module and only :app knows
 * the deployed backend URL for this build variant/flavor.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {

    @Provides
    @Singleton
    @BaseUrl
    fun provideBaseUrl(): String = BuildConfig.UPLOAD_BASE_URL
}
