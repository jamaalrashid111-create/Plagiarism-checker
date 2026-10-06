package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlagiarismRequest(
    @Json(name = "text") val text: String,
    @Json(name = "language") val language: String = "en"
)

@JsonClass(generateAdapter = true)
data class PlagiarismSource(
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String,
    @Json(name = "domain") val domain: String,
    @Json(name = "matchedWords") val matchedWords: Int = 0,
    @Json(name = "similarity") val similarity: Int = 0
)

@JsonClass(generateAdapter = true)
data class PlagiarismMatch(
    @Json(name = "text") val text: String,
    @Json(name = "sourceUrl") val sourceUrl: String? = null,
    @Json(name = "sourceTitle") val sourceTitle: String? = null,
    @Json(name = "similarity") val similarity: Int = 0,
    @Json(name = "start") val start: Int = 0,
    @Json(name = "end") val end: Int = 0
)

@JsonClass(generateAdapter = true)
data class GeminiAnalysisResult(
    @Json(name = "similarity_score") val similarityScore: String = "0%",
    @Json(name = "originality_score") val originalityScore: String = "100%",
    @Json(name = "status") val status: String = "Mostly Original",
    @Json(name = "analysis_breakdown") val analysisBreakdown: String = ""
)

@JsonClass(generateAdapter = true)
data class PlagiarismResponse(
    @Json(name = "jobId") val jobId: String,
    @Json(name = "status") val status: String, // "completed", "processing", "queued"
    @Json(name = "plagiarismPercentage") val plagiarismPercentage: Int = 0,
    @Json(name = "originalPercentage") val originalPercentage: Int = 100,
    @Json(name = "wordCount") val wordCount: Int = 0,
    @Json(name = "matchedWordCount") val matchedWordCount: Int = 0,
    @Json(name = "sources") val sources: List<PlagiarismSource> = emptyList(),
    @Json(name = "matches") val matches: List<PlagiarismMatch> = emptyList(),
    @Json(name = "analysisBreakdown") val analysisBreakdown: String? = null
)

data class DocumentStats(
    val wordCount: Int,
    val charCount: Int,
    val sentenceCount: Int,
    val paragraphCount: Int
)

sealed class DocumentExtractionResult {
    data class Success(val text: String, val fileName: String, val stats: DocumentStats) : DocumentExtractionResult()
    data class EmptyText(val fileName: String) : DocumentExtractionResult()
    data class ScannedPdfNeedsOcr(val message: String) : DocumentExtractionResult()
    data class Error(val message: String) : DocumentExtractionResult()
}

enum class CheckingStep(val label: String) {
    ANALYZING("Analyzing your content..."),
    SEARCHING("Searching for matching content..."),
    COMPARING("Comparing sources...")
}

enum class PlanType(val displayName: String, val wordLimit: Int, val dailyChecksLimit: Int) {
    FREE("Free Plan", 1500, 5),
    PRO("Munasar Pro", 25000, 100)
}

data class UserProfile(
    val isLoggedIn: Boolean = false,
    val email: String? = null,
    val displayName: String = "Guest User",
    val plan: PlanType = PlanType.FREE,
    val checksUsedToday: Int = 0
)
