package com.example

import com.example.data.model.PlagiarismMatch
import com.example.data.model.PlagiarismResponse
import com.example.data.model.PlagiarismSource
import com.example.data.remote.PlagiarismProvider
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PlagiarismServiceTest {

    private class MockUnconfiguredProvider : PlagiarismProvider {
        override fun isConfigured(): Boolean = false
        override fun getConfigurationStatus(): String = "Plagiarism checking service is not configured yet."
        override suspend fun checkText(text: String, language: String): Result<PlagiarismResponse> {
            return Result.failure(IllegalStateException("Plagiarism checking service is not configured yet."))
        }
        override suspend fun checkDocument(file: File, language: String): Result<PlagiarismResponse> {
            return Result.failure(IllegalStateException("Plagiarism checking service is not configured yet."))
        }
        override suspend fun getResult(jobId: String): Result<PlagiarismResponse> {
            return Result.failure(IllegalStateException("Plagiarism checking service is not configured yet."))
        }
        override suspend fun cancelCheck(jobId: String): Result<Unit> = Result.success(Unit)
    }

    private class MockConfiguredProvider : PlagiarismProvider {
        override fun isConfigured(): Boolean = true
        override fun getConfigurationStatus(): String = "Configured"
        override suspend fun checkText(text: String, language: String): Result<PlagiarismResponse> {
            return Result.success(
                PlagiarismResponse(
                    jobId = "test-job-123",
                    status = "completed",
                    plagiarismPercentage = 23,
                    originalPercentage = 77,
                    wordCount = 100,
                    matchedWordCount = 23,
                    sources = listOf(
                        PlagiarismSource(
                            title = "Journal of Modern Software",
                            url = "https://example.com/paper",
                            domain = "example.com",
                            matchedWords = 23,
                            similarity = 85
                        )
                    ),
                    matches = listOf(
                        PlagiarismMatch(
                            text = "Matching passage from example paper.",
                            sourceUrl = "https://example.com/paper",
                            sourceTitle = "Journal of Modern Software",
                            similarity = 85,
                            start = 0,
                            end = 35
                        )
                    )
                )
            )
        }
        override suspend fun checkDocument(file: File, language: String): Result<PlagiarismResponse> {
            return checkText("sample")
        }
        override suspend fun getResult(jobId: String): Result<PlagiarismResponse> {
            return checkText("sample")
        }
        override suspend fun cancelCheck(jobId: String): Result<Unit> = Result.success(Unit)
    }

    @Test
    fun testUnconfiguredProviderFailsWithExactMessage() = runBlocking {
        val provider = MockUnconfiguredProvider()
        assertFalse(provider.isConfigured())
        assertEquals("Plagiarism checking service is not configured yet.", provider.getConfigurationStatus())

        val result = provider.checkText("Sample text to check")
        assertTrue(result.isFailure)
        assertEquals("Plagiarism checking service is not configured yet.", result.exceptionOrNull()?.message)
    }

    @Test
    fun testConfiguredProviderResponseSerialization() {
        val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(PlagiarismResponse::class.java)

        val json = """
            {
              "jobId": "job-abc",
              "status": "completed",
              "plagiarismPercentage": 23,
              "originalPercentage": 77,
              "wordCount": 1000,
              "matchedWordCount": 230,
              "sources": [
                {
                  "title": "Example Source",
                  "url": "https://example.com",
                  "domain": "example.com",
                  "matchedWords": 120,
                  "similarity": 85
                }
              ],
              "matches": [
                {
                  "text": "matching text snippet",
                  "sourceUrl": "https://example.com",
                  "sourceTitle": "Example Source",
                  "similarity": 85,
                  "start": 100,
                  "end": 180
                }
              ]
            }
        """.trimIndent()

        val parsed = adapter.fromJson(json)
        assertTrue(parsed != null)
        assertEquals("job-abc", parsed?.jobId)
        assertEquals(23, parsed?.plagiarismPercentage)
        assertEquals(77, parsed?.originalPercentage)
        assertEquals(1, parsed?.sources?.size)
        assertEquals("example.com", parsed?.sources?.firstOrNull()?.domain)
        assertEquals(1, parsed?.matches?.size)
    }
}
