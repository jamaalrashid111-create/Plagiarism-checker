package com.example.data.remote

import com.example.data.model.PlagiarismRequest
import com.example.data.model.PlagiarismResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PlagiarismApiService {
    @POST("api/plagiarism/check")
    suspend fun checkText(
        @Header("Authorization") authHeader: String?,
        @Header("X-API-Key") apiKeyHeader: String?,
        @Body request: PlagiarismRequest
    ): Response<PlagiarismResponse>

    @Multipart
    @POST("api/plagiarism/check-document")
    suspend fun checkDocument(
        @Header("Authorization") authHeader: String?,
        @Header("X-API-Key") apiKeyHeader: String?,
        @Part file: MultipartBody.Part,
        @Part("language") language: RequestBody
    ): Response<PlagiarismResponse>

    @GET("api/plagiarism/results/{jobId}")
    suspend fun getResult(
        @Header("Authorization") authHeader: String?,
        @Header("X-API-Key") apiKeyHeader: String?,
        @Path("jobId") jobId: String
    ): Response<PlagiarismResponse>

    @POST("api/plagiarism/cancel/{jobId}")
    suspend fun cancelCheck(
        @Header("Authorization") authHeader: String?,
        @Header("X-API-Key") apiKeyHeader: String?,
        @Path("jobId") jobId: String
    ): Response<Unit>
}
