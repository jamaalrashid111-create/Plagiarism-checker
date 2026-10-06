package com.example.data.remote

import com.example.data.local.SettingsPreferences
import com.example.data.model.GeminiAnalysisResult
import com.example.data.model.PlagiarismMatch
import com.example.data.model.PlagiarismResponse
import com.example.data.model.PlagiarismSource
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Direct Gemini API analysis provider for Munasar Plagiarism Checker.
 * Uses endpoint: https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
 */
class GeminiPlagiarismProvider(
    private val preferences: SettingsPreferences
) : PlagiarismProvider {

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val resultAdapter = moshi.adapter(GeminiAnalysisResult::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
        You are the core analysis engine for "Munasar Plagiarism Checker", a professional text verification and originality analysis tool. Your job is to analyze user-submitted text for originality, paraphrasing, matching patterns, and potential AI generation.

        Always return your analysis in a clean, structured JSON format with the following keys:
        - "similarity_score": a percentage estimation (e.g., "12%")
        - "originality_score": a percentage estimation (e.g., "88%")
        - "status": a brief assessment (e.g., "Mostly Original", "Potential Paraphrasing Detected")
        - "analysis_breakdown": a concise summary explaining potential matches, phrasing concerns, or structural patterns found in the text.
    """.trimIndent()

    override fun isConfigured(): Boolean {
        return preferences.isGeminiConfigured()
    }

    override fun getConfigurationStatus(): String {
        return if (isConfigured()) {
            "Connected: Gemini 2.5 Flash Engine"
        } else {
            "Plagiarism checking service is not configured yet."
        }
    }

    override suspend fun checkText(text: String, language: String): Result<PlagiarismResponse> =
        withContext(Dispatchers.IO) {
            val apiKey = preferences.getGeminiApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Plagiarism checking service is not configured yet.")
                )
            }

            try {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

                val requestPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", "Please analyze the following text for plagiarism, similarity, and originality:\n\n$text")
                                })
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemPrompt)
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("responseMimeType", "application/json")
                    })
                }

                val body = requestPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(endpoint)
                    .post(body)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBodyStr = response.body?.string()

                if (!response.isSuccessful || responseBodyStr == null) {
                    val errorMsg = when (response.code) {
                        400 -> "Invalid request sent to Gemini API."
                        401, 403 -> "Gemini API authentication failed. Please verify your API key in Settings."
                        429 -> "Gemini API rate limit reached. Please wait a moment before retrying."
                        503 -> "Gemini service temporarily unavailable. Please try again shortly."
                        else -> "Analysis request failed with code ${response.code}."
                    }
                    return@withContext Result.failure(IOException(errorMsg))
                }

                // Parse Gemini candidate JSON
                val rootJson = JSONObject(responseBodyStr)
                val candidates = rootJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val contentObj = firstCandidate?.optJSONObject("content")
                val parts = contentObj?.optJSONArray("parts")
                val textOutput = parts?.optJSONObject(0)?.optString("text")

                if (textOutput.isNullOrBlank()) {
                    return@withContext Result.failure(IOException("Empty analysis received from Gemini engine."))
                }

                // Parse structured JSON analysis
                val analysisResult = try {
                    resultAdapter.fromJson(textOutput)
                } catch (_: Exception) {
                    // Fallback JSON parser if format varies slightly
                    val innerObj = JSONObject(textOutput)
                    GeminiAnalysisResult(
                        similarityScore = innerObj.optString("similarity_score", "0%"),
                        originalityScore = innerObj.optString("originality_score", "100%"),
                        status = innerObj.optString("status", "Analyzed"),
                        analysisBreakdown = innerObj.optString("analysis_breakdown", "")
                    )
                } ?: GeminiAnalysisResult()

                val similarityInt = parsePercentage(analysisResult.similarityScore)
                val originalityInt = parsePercentage(analysisResult.originalityScore).let {
                    if (it == 0 && similarityInt > 0) (100 - similarityInt).coerceAtLeast(0) else it
                }

                val words = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
                val wordCount = words.size
                val matchedWords = ((wordCount * similarityInt) / 100).coerceAtLeast(0)

                val sources = mutableListOf<PlagiarismSource>()
                val matches = mutableListOf<PlagiarismMatch>()

                if (similarityInt > 0) {
                    sources.add(
                        PlagiarismSource(
                            title = "Munasar Web Index & Synthesis Match",
                            url = "https://www.munasar.online/verify",
                            domain = "munasar.online",
                            matchedWords = matchedWords,
                            similarity = similarityInt
                        )
                    )
                }

                if (analysisResult.analysisBreakdown.isNotBlank()) {
                    matches.add(
                        PlagiarismMatch(
                            text = analysisResult.analysisBreakdown,
                            sourceTitle = "Engine Assessment: ${analysisResult.status}",
                            sourceUrl = "https://www.munasar.online/",
                            similarity = similarityInt,
                            start = 0,
                            end = text.length
                        )
                    )
                }

                Result.success(
                    PlagiarismResponse(
                        jobId = UUID.randomUUID().toString(),
                        status = analysisResult.status,
                        plagiarismPercentage = similarityInt,
                        originalPercentage = originalityInt,
                        wordCount = wordCount,
                        matchedWordCount = matchedWords,
                        sources = sources,
                        matches = matches,
                        analysisBreakdown = analysisResult.analysisBreakdown
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parsePercentage(raw: String): Int {
        val digits = raw.replace("%", "").trim()
        return digits.toIntOrNull() ?: 0
    }

    override suspend fun checkDocument(file: File, language: String): Result<PlagiarismResponse> {
        val text = file.readText()
        return checkText(text, language)
    }

    override suspend fun getResult(jobId: String): Result<PlagiarismResponse> {
        return Result.failure(UnsupportedOperationException("Synchronous analysis engine."))
    }

    override suspend fun cancelCheck(jobId: String): Result<Unit> {
        return Result.success(Unit)
    }
}
