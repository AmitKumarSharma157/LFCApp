package com.example.lfcapp.data.repository

import android.util.Log
import com.example.lfcapp.domain.model.ImageUploadRequest
import com.example.lfcapp.domain.model.ProcessedImage
import com.example.lfcapp.domain.repository.ImageProcessingRepository
import com.example.lfcapp.domain.util.DomainResult
import com.example.lfcapp.network.api.ApiService
import com.example.lfcapp.network.dto.ImageProcessingRequest
import com.example.lfcapp.network.handler.NetworkResult
import com.example.lfcapp.network.handler.safeApiCall
import java.util.UUID
import javax.inject.Inject

/**
 * Data-layer implementation. Maps the domain request to the network DTO,
 * performs the call via the single [ApiService] using [safeApiCall], and
 * translates the resulting [NetworkResult] into a [DomainResult] so callers
 * above this layer never touch Retrofit/HTTP types.
 */
class ImageProcessingRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ImageProcessingRepository {

    override suspend fun processImage(request: ImageUploadRequest): DomainResult<ProcessedImage> {
        val dto = ImageProcessingRequest(
            UnitId = request.UnitId,
            PrinterName = request.PrinterName,
            UserData = request.UserData,
            MessageId = UUID.randomUUID().toString(),
            Image = request.Image
        )

        Log.d("ImageProcessingRepository", "processImage: dto=$dto")

        return when (val result = safeApiCall { apiService.processImage(dto) }) {
            is NetworkResult.Success -> {
                val status = result.data.Status
                if (status.isNullOrBlank()) {
                    DomainResult.Failure(message = "Response did not contain a Status value")
                } else {
                    DomainResult.Success(
                        ProcessedImage(Status = status, ParsedID = result.data.ParsedID.orEmpty())
                    )
                }
            }

            is NetworkResult.ApiError -> DomainResult.Failure(
                message = "Request failed with HTTP ${result.code}: ${result.errorBody}"
            )

            is NetworkResult.NetworkError -> DomainResult.Failure(result.message, result.cause)
            is NetworkResult.UnknownError -> DomainResult.Failure(result.message, result.cause)
        }
    }
}
