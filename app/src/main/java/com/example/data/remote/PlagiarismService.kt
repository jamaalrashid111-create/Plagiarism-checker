package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.data.local.SettingsPreferences
import com.example.data.model.PlagiarismResponse
import com.example.data.model.PlanType
import java.io.File

class PlagiarismService(
    private val context: Context,
    private val provider: PlagiarismProvider,
    private val preferences: SettingsPreferences
) {

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun isConfigured(): Boolean = provider.isConfigured()

    fun getConfigurationStatus(): String = provider.getConfigurationStatus()

    suspend fun checkText(text: String, language: String = "en"): Result<PlagiarismResponse> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("No text entered."))
        }

        val words = trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (words.isEmpty()) {
            return Result.failure(IllegalArgumentException("Document is empty."))
        }

        val plan = preferences.getPlanType()
        if (words.size > plan.wordLimit) {
            return Result.failure(
                IllegalArgumentException(
                    "Text contains ${words.size} words. Your current ${plan.displayName} limit is ${plan.wordLimit} words. Upgrade to Pro for higher limits."
                )
            )
        }

        val checksToday = preferences.getChecksUsedToday()
        if (checksToday >= plan.dailyChecksLimit) {
            return Result.failure(
                IllegalArgumentException(
                    "You have reached your daily limit of ${plan.dailyChecksLimit} checks. Upgrade to Pro for more checks."
                )
            )
        }

        if (!isInternetAvailable()) {
            return Result.failure(
                IllegalStateException("An internet connection is required to check plagiarism.")
            )
        }

        if (!provider.isConfigured()) {
            return Result.failure(
                IllegalStateException("Plagiarism checking service is not configured yet.")
            )
        }

        val result = provider.checkText(trimmed, language)
        if (result.isSuccess) {
            preferences.incrementChecksUsedToday()
        }
        return result
    }

    suspend fun checkDocument(file: File, language: String = "en"): Result<PlagiarismResponse> {
        if (!file.exists() || file.length() == 0L) {
            return Result.failure(IllegalArgumentException("Document is empty."))
        }

        if (file.length() > 25 * 1024 * 1024) {
            return Result.failure(IllegalArgumentException("Document is too large. Maximum file size is 25MB."))
        }

        if (!isInternetAvailable()) {
            return Result.failure(
                IllegalStateException("An internet connection is required to check plagiarism.")
            )
        }

        if (!provider.isConfigured()) {
            return Result.failure(
                IllegalStateException("Plagiarism checking service is not configured yet.")
            )
        }

        val result = provider.checkDocument(file, language)
        if (result.isSuccess) {
            preferences.incrementChecksUsedToday()
        }
        return result
    }

    suspend fun getResults(jobId: String): Result<PlagiarismResponse> {
        return provider.getResult(jobId)
    }

    suspend fun cancelCheck(jobId: String): Result<Unit> {
        return provider.cancelCheck(jobId)
    }
}
