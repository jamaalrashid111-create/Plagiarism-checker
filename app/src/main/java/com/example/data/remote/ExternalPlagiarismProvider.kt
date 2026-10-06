package com.example.data.remote

import com.example.data.local.SettingsPreferences
import com.example.data.model.PlagiarismRequest
import com.example.data.model.PlagiarismResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Production implementation of PlagiarismProvider.
 * Connects to external plagiarism detection backend configured via
 * PLAGIARISM_API_URL and PLAGIARISM_API_KEY (or Settings).
 */
class ExternalPlagiarismProvider(
    private val preferences: SettingsPreferences
) : PlagiarismProvider {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val baseOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val geminiProvider = GeminiPlagiarismProvider(preferences)

    override fun isConfigured(): Boolean {
        return preferences.isGeminiConfigured() || (preferences.getApiUrl().isNotBlank() && preferences.getApiUrl().startsWith("http"))
    }

    override fun getConfigurationStatus(): String {
        return if (preferences.isGeminiConfigured()) {
            "Connected: Gemini 2.5 Flash Engine"
        } else if (preferences.getApiUrl().isNotBlank()) {
            "Configured: ${preferences.getApiUrl()}"
        } else {
            "Plagiarism checking service is not configured yet."
        }
    }

    private fun getApiService(): PlagiarismApiService {
        var baseUrl = preferences.getApiUrl().trim()
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(baseOkHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PlagiarismApiService::class.java)
    }

    private fun getAuthHeaders(): Pair<String?, String?> {
        val key = preferences.getApiKey()
        if (key.isBlank()) return Pair(null, null)
        val authHeader = if (key.startsWith("Bearer ", ignoreCase = true)) key else "Bearer $key"
        return Pair(authHeader, key)
    }

    override suspend fun checkText(text: String, language: String): Result<PlagiarismResponse> =
        withContext(Dispatchers.IO) {
            if (preferences.isGeminiConfigured()) {
                return@withContext geminiProvider.checkText(text, language)
            }

            if (!isConfigured()) {
                return@withContext Result.failure(
                    IllegalStateException("Plagiarism checking service is not configured yet.")
                )
            }

            try {
                val service = getApiService()
                val (authHeader, apiKeyHeader) = getAuthHeaders()
                val response = service.checkText(authHeader, apiKeyHeader, PlagiarismRequest(text, language))

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Result.success(body)
                    } else {
                        Result.failure(IOException("Received empty response from plagiarism server."))
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401, 403 -> "API authentication failed. Please verify your API key in Settings."
                        429 -> "API rate limit reached. Please wait before retrying."
                        502, 503, 504 -> "Plagiarism service is unavailable. Please try again later."
                        else -> "API request failed with error code ${response.code()}."
                    }
                    Result.failure(IOException(errorMessage))
                }
            } catch (e: IOException) {
                Result.failure(IOException("Network connection unavailable or connection timed out."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun checkDocument(file: File, language: String): Result<PlagiarismResponse> =
        withContext(Dispatchers.IO) {
            if (preferences.isGeminiConfigured()) {
                return@withContext geminiProvider.checkDocument(file, language)
            }

            if (!isConfigured()) {
                return@withContext Result.failure(
                    IllegalStateException("Plagiarism checking service is not configured yet.")
                )
            }

            try {
                val service = getApiService()
                val (authHeader, apiKeyHeader) = getAuthHeaders()
                val requestFile = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val langPart = language.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = service.checkDocument(authHeader, apiKeyHeader, body, langPart)
                if (response.isSuccessful) {
                    val respBody = response.body()
                    if (respBody != null) {
                        Result.success(respBody)
                    } else {
                        Result.failure(IOException("Received empty response from plagiarism server."))
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401, 403 -> "API authentication failed."
                        502, 503 -> "Plagiarism service is unavailable."
                        else -> "API request failed (${response.code()})."
                    }
                    Result.failure(IOException(errorMessage))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getResult(jobId: String): Result<PlagiarismResponse> =
        withContext(Dispatchers.IO) {
            if (!isConfigured()) {
                return@withContext Result.failure(
                    IllegalStateException("Plagiarism checking service is not configured yet.")
                )
            }

            try {
                val service = getApiService()
                val (authHeader, apiKeyHeader) = getAuthHeaders()
                val response = service.getResult(authHeader, apiKeyHeader, jobId)

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(IOException("Failed to retrieve check status (${response.code()})."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun cancelCheck(jobId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            if (!isConfigured()) return@withContext Result.success(Unit)

            try {
                val service = getApiService()
                val (authHeader, apiKeyHeader) = getAuthHeaders()
                service.cancelCheck(authHeader, apiKeyHeader, jobId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
