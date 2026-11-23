package com.example.lfcapp.data.di

import com.example.lfcapp.data.repository.ImageProcessingRepositoryImpl
import com.example.lfcapp.domain.repository.ImageProcessingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindImageProcessingRepository(
        impl: ImageProcessingRepositoryImpl
    ): ImageProcessingRepository
}
