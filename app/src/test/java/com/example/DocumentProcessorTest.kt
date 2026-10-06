package com.example

import com.example.data.model.DocumentExtractionResult
import com.example.domain.DocumentProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class DocumentProcessorTest {

    @Test
    fun testWordCountAndStatsCalculation() {
        val sampleText = "Artificial intelligence is transforming education. Plagiarism detection tools ensure academic integrity! Can they detect rephrased content? Yes, indeed."
        val stats = DocumentProcessor.calculateStats(sampleText)

        assertTrue(stats.wordCount > 15)
        assertEquals(sampleText.length, stats.charCount)
        assertEquals(4, stats.sentenceCount)
        assertEquals(1, stats.paragraphCount)
    }

    @Test
    fun testEmptyTextStats() {
        val stats = DocumentProcessor.calculateStats("   \n\t  ")
        assertEquals(0, stats.wordCount)
        assertEquals(0, stats.charCount)
        assertEquals(0, stats.sentenceCount)
        assertEquals(0, stats.paragraphCount)
    }

    @Test
    fun testExtractTxtSuccess() {
        val content = "This is a clean academic essay submitted for plagiarism verification."
        val inputStream = ByteArrayInputStream(content.toByteArray(Charsets.UTF_8))
        val result = DocumentProcessor.extractTxt(inputStream, "essay.txt")

        assertTrue(result is DocumentExtractionResult.Success)
        val success = result as DocumentExtractionResult.Success
        assertEquals("essay.txt", success.fileName)
        assertEquals(10, success.stats.wordCount)
    }

    @Test
    fun testExtractTxtEmpty() {
        val inputStream = ByteArrayInputStream("".toByteArray(Charsets.UTF_8))
        val result = DocumentProcessor.extractTxt(inputStream, "empty.txt")

        assertTrue(result is DocumentExtractionResult.EmptyText)
    }
}
