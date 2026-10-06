package com.example.domain

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.DocumentExtractionResult
import com.example.data.model.DocumentStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.Inflater
import java.util.zip.ZipInputStream

object DocumentProcessor {

    fun calculateStats(rawText: String): DocumentStats {
        val text = rawText.trim()
        if (text.isEmpty()) {
            return DocumentStats(wordCount = 0, charCount = 0, sentenceCount = 0, paragraphCount = 0)
        }

        val words = text.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val charCount = text.length

        val paragraphs = text.split(Regex("(\r?\n){2,}"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val paragraphCount = if (paragraphs.isEmpty()) (if (text.isNotEmpty()) 1 else 0) else paragraphs.size

        val sentenceDelimiters = Regex("[.!?]+(\\s+|$)")
        val sentences = text.split(sentenceDelimiters).filter { it.trim().isNotEmpty() }
        val sentenceCount = if (sentences.isEmpty()) (if (text.isNotEmpty()) 1 else 0) else sentences.size

        return DocumentStats(
            wordCount = words.size,
            charCount = charCount,
            sentenceCount = sentenceCount,
            paragraphCount = paragraphCount
        )
    }

    suspend fun processUri(context: Context, uri: Uri): DocumentExtractionResult =
        withContext(Dispatchers.IO) {
            try {
                val fileName = getFileName(context, uri) ?: "Document"
                val extension = fileName.substringAfterLast('.', "").lowercase()

                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext DocumentExtractionResult.Error("Could not open file: $fileName")

                inputStream.use { stream ->
                    when (extension) {
                        "txt" -> extractTxt(stream, fileName)
                        "docx" -> extractDocx(stream, fileName)
                        "pdf" -> extractPdf(stream, fileName)
                        else -> DocumentExtractionResult.Error("Document format '.$extension' is not supported. Please select PDF, DOCX, or TXT.")
                    }
                }
            } catch (e: Exception) {
                DocumentExtractionResult.Error(e.message ?: "Could not extract text from this document.")
            }
        }

    fun extractTxt(stream: InputStream, fileName: String): DocumentExtractionResult {
        val text = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }.trim()
        if (text.isEmpty()) {
            return DocumentExtractionResult.EmptyText(fileName)
        }
        val stats = calculateStats(text)
        return DocumentExtractionResult.Success(text, fileName, stats)
    }

    fun extractDocx(stream: InputStream, fileName: String): DocumentExtractionResult {
        return try {
            val zipStream = ZipInputStream(stream)
            var entry = zipStream.nextEntry
            var documentXmlBytes: ByteArray? = null

            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    val buffer = ByteArrayOutputStream()
                    val data = ByteArray(4096)
                    var count: Int
                    while (zipStream.read(data).also { count = it } != -1) {
                        buffer.write(data, 0, count)
                    }
                    documentXmlBytes = buffer.toByteArray()
                    break
                }
                zipStream.closeEntry()
                entry = zipStream.nextEntry
            }

            if (documentXmlBytes == null) {
                return DocumentExtractionResult.Error("Invalid or corrupted Word document ($fileName).")
            }

            val textBuilder = StringBuilder()
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(ByteArrayInputStream(documentXmlBytes), "UTF-8")

            var eventType = parser.eventType
            var inParagraph = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val name = parser.name
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (name) {
                            "p" -> {
                                inParagraph = true
                            }
                            "br", "cr" -> {
                                textBuilder.append("\n")
                            }
                            "tab" -> {
                                textBuilder.append("\t")
                            }
                            "t" -> {
                                val text = parser.nextText()
                                textBuilder.append(text)
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (name == "p") {
                            textBuilder.append("\n\n")
                            inParagraph = false
                        }
                    }
                }
                eventType = parser.next()
            }

            val text = textBuilder.toString().trim()
            if (text.isEmpty()) {
                DocumentExtractionResult.EmptyText(fileName)
            } else {
                val stats = calculateStats(text)
                DocumentExtractionResult.Success(text, fileName, stats)
            }
        } catch (e: Exception) {
            DocumentExtractionResult.Error("Could not extract text from DOCX: ${e.localizedMessage}")
        }
    }

    fun extractPdf(stream: InputStream, fileName: String): DocumentExtractionResult {
        return try {
            val bytes = stream.readBytes()
            val text = extractTextFromPdfBytes(bytes).trim()

            if (text.isEmpty() || countLetters(text) < 5) {
                // Scanned PDF detection
                DocumentExtractionResult.ScannedPdfNeedsOcr(
                    "This PDF does not contain selectable text. OCR support is required to scan this document."
                )
            } else {
                val stats = calculateStats(text)
                DocumentExtractionResult.Success(text, fileName, stats)
            }
        } catch (e: Exception) {
            DocumentExtractionResult.Error("Could not extract text from PDF: ${e.localizedMessage}")
        }
    }

    private fun countLetters(text: String): Int {
        var count = 0
        for (ch in text) {
            if (ch.isLetter()) count++
        }
        return count
    }

    /**
     * Extracts text tokens from PDF streams.
     * Decodes text chunks within BT ... ET blocks and FlateDecode streams.
     */
    private fun extractTextFromPdfBytes(pdfBytes: ByteArray): String {
        val extractedBuilder = StringBuilder()
        val textStreams = mutableListOf<ByteArray>()

        // Find streams in PDF
        var idx = 0
        val streamMarker = "stream".toByteArray(Charsets.US_ASCII)
        val endStreamMarker = "endstream".toByteArray(Charsets.US_ASCII)

        while (idx < pdfBytes.size) {
            val streamPos = indexOfBytes(pdfBytes, streamMarker, idx)
            if (streamPos == -1) break

            var startData = streamPos + streamMarker.size
            if (startData < pdfBytes.size && pdfBytes[startData] == '\r'.code.toByte()) startData++
            if (startData < pdfBytes.size && pdfBytes[startData] == '\n'.code.toByte()) startData++

            val endStreamPos = indexOfBytes(pdfBytes, endStreamMarker, startData)
            if (endStreamPos == -1) break

            val streamData = pdfBytes.copyOfRange(startData, endStreamPos)
            textStreams.add(streamData)

            idx = endStreamPos + endStreamMarker.size
        }

        for (stream in textStreams) {
            val decompressed = tryDecompressFlate(stream) ?: stream
            parsePdfStreamText(decompressed, extractedBuilder)
        }

        // Fallback: Also search raw string occurrences in the uncompressed file if no stream parsed
        if (extractedBuilder.isEmpty()) {
            parsePdfStreamText(pdfBytes, extractedBuilder)
        }

        return cleanPdfExtractedText(extractedBuilder.toString())
    }

    private fun tryDecompressFlate(data: ByteArray): ByteArray? {
        val inflater = Inflater(false)
        inflater.setInput(data)
        val outputStream = ByteArrayOutputStream(data.size * 2)
        val buffer = ByteArray(4096)
        try {
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0) {
                    if (inflater.needsInput() || inflater.needsDictionary()) break
                }
                outputStream.write(buffer, 0, count)
            }
            inflater.end()
            return outputStream.toByteArray()
        } catch (_: Exception) {
            inflater.end()
            // Try with nowrap=true
            val rawInflater = Inflater(true)
            rawInflater.setInput(data)
            val rawOutput = ByteArrayOutputStream(data.size * 2)
            return try {
                while (!rawInflater.finished()) {
                    val count = rawInflater.inflate(buffer)
                    if (count == 0) break
                    rawOutput.write(buffer, 0, count)
                }
                rawInflater.end()
                rawOutput.toByteArray()
            } catch (_: Exception) {
                rawInflater.end()
                null
            }
        }
    }

    private fun parsePdfStreamText(data: ByteArray, out: StringBuilder) {
        val content = String(data, Charsets.ISO_8859_1)
        var i = 0
        while (i < content.length) {
            val btIdx = content.indexOf("BT", i)
            if (btIdx == -1) break

            val etIdx = content.indexOf("ET", btIdx + 2)
            val chunk = if (etIdx != -1) content.substring(btIdx + 2, etIdx) else content.substring(btIdx + 2)

            extractTextFromBlock(chunk, out)

            if (etIdx == -1) break
            i = etIdx + 2
        }
    }

    private fun extractTextFromBlock(block: String, out: StringBuilder) {
        var pos = 0
        while (pos < block.length) {
            val ch = block[pos]
            if (ch == '(') {
                // Literal string
                val str = StringBuilder()
                var parenDepth = 1
                pos++
                while (pos < block.length && parenDepth > 0) {
                    val c = block[pos]
                    if (c == '\\' && pos + 1 < block.length) {
                        pos++
                        when (val esc = block[pos]) {
                            'n' -> str.append('\n')
                            'r' -> str.append('\r')
                            't' -> str.append('\t')
                            'b' -> str.append('\b')
                            'f' -> str.append('\u000C')
                            '(', ')', '\\' -> str.append(esc)
                            else -> str.append(esc)
                        }
                    } else if (c == '(') {
                        parenDepth++
                        str.append(c)
                    } else if (c == ')') {
                        parenDepth--
                        if (parenDepth > 0) str.append(c)
                    } else {
                        str.append(c)
                    }
                    pos++
                }
                out.append(str).append(" ")
            } else if (ch == '<' && pos + 1 < block.length && block[pos + 1] != '<') {
                // Hex string
                val endHex = block.indexOf('>', pos + 1)
                if (endHex != -1) {
                    val hexStr = block.substring(pos + 1, endHex).replace("\\s".toRegex(), "")
                    out.append(decodeHexPdfString(hexStr)).append(" ")
                    pos = endHex + 1
                } else {
                    pos++
                }
            } else if (ch == 'T' && pos + 1 < block.length && (block[pos + 1] == '*' || block[pos + 1] == 'J')) {
                out.append("\n")
                pos += 2
            } else {
                pos++
            }
        }
    }

    private fun decodeHexPdfString(hex: String): String {
        return try {
            val sb = java.lang.StringBuilder()
            var i = 0
            while (i < hex.length) {
                if (i + 1 < hex.length) {
                    val code = hex.substring(i, i + 2).toInt(16)
                    if (code in 32..126 || code == 10 || code == 13) {
                        sb.append(code.toChar())
                    }
                }
                i += 2
            }
            sb.toString()
        } catch (_: Exception) {
            ""
        }
    }

    private fun cleanPdfExtractedText(raw: String): String {
        return raw.replace("\r", "")
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    private fun indexOfBytes(source: ByteArray, target: ByteArray, fromIndex: Int): Int {
        if (target.isEmpty() || fromIndex >= source.size) return -1
        outer@ for (i in fromIndex..(source.size - target.size)) {
            for (j in target.indices) {
                if (source[i + j] != target[j]) continue@outer
            }
            return i
        }
        return -1
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) name = cursor.getString(idx)
                }
            }
        }
        return name ?: uri.lastPathSegment
    }
}
