package com.example.data.remote

import com.example.data.model.PlagiarismResponse
import java.io.File

/**
 * Plagiarism detection provider interface.
 * Decouples the UI and business logic from specific backend plagiarism engines.
 */
interface PlagiarismProvider {
    /**
     * Checks whether the backend provider is properly configured with an API URL / Key.
     */
    fun isConfigured(): Boolean

    /**
     * Gets the current configuration status message.
     */
    fun getConfigurationStatus(): String

    /**
     * Submits text for plagiarism analysis.
     */
    suspend fun checkText(text: String, language: String = "en"): Result<PlagiarismResponse>

    /**
     * Submits a document file for plagiarism analysis.
     */
    suspend fun checkDocument(file: File, language: String = "en"): Result<PlagiarismResponse>

    /**
     * Queries results for an asynchronous check job.
     */
    suspend fun getResult(jobId: String): Result<PlagiarismResponse>

    /**
     * Cancels an ongoing check job on the server.
     */
    suspend fun cancelCheck(jobId: String): Result<Unit>
}
