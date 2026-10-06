package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.MatchedPassageCard
import com.example.ui.components.ScoreCircularBadge
import com.example.ui.components.SummaryStatsGrid
import com.example.ui.theme.MunasarBlueContainer
import com.example.ui.theme.MunasarBluePrimary
import com.example.ui.theme.MunasarNavyText
import com.example.ui.theme.OriginalSuccessGreen
import com.example.ui.theme.PlagiarismAlertRed

@Composable
fun ResultsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val result = uiState.currentResult

    if (result == null) {
        // Empty state
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("results_empty_state"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MunasarBluePrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Active Plagiarism Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MunasarNavyText
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Paste text or import a document on the Home screen to run a plagiarism scan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.selectTab(NavigationTab.HOME) },
                    colors = ButtonDefaults.buttonColors(containerColor = MunasarBluePrimary)
                ) {
                    Text("Go to Home")
                }
            }
        }
        return
    }

    val totalWords = result.wordCount.takeIf { it > 0 } ?: uiState.documentStats.wordCount
    val originalWords = (totalWords - result.matchedWordCount).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("results_screen")
    ) {
        // Document Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Plagiarism Report",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MunasarNavyText
                )
                Text(
                    text = uiState.currentDocumentName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // PDF Export Button
            OutlinedButton(
                onClick = {
                    viewModel.exportPdfReport(context) { _, uri ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier.testTag("export_pdf_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("PDF")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Circular Score Card
        ScoreCircularBadge(
            plagiarismPercentage = result.plagiarismPercentage,
            originalPercentage = result.originalPercentage
        )

        if (!result.analysisBreakdown.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("analysis_breakdown_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MunasarBlueContainer)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ASSESSMENT: ${result.status.uppercase()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MunasarBluePrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Analysis Breakdown",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MunasarNavyText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = result.analysisBreakdown,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Stats Grid
        SummaryStatsGrid(
            totalWords = totalWords,
            matchedWords = result.matchedWordCount,
            sourcesCount = result.sources.size,
            originalWords = originalWords
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Action Navigation Bar (Sources & Share)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.toggleSourcesScreen(true) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("view_sources_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MunasarBluePrimary)
            ) {
                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sources (${result.sources.size})")
            }

            OutlinedButton(
                onClick = {
                    val shareText = "Munasar Plagiarism Report for '${uiState.currentDocumentName}':\n" +
                            "• Plagiarism: ${result.plagiarismPercentage}%\n" +
                            "• Original: ${result.originalPercentage}%\n" +
                            "• Total Words: $totalWords\n" +
                            "• Sources Found: ${result.sources.size}\n\n" +
                            "Verified via Munasar Plagiarism Checker\nhttps://www.munasar.online/"

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Munasar Plagiarism Report")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Plagiarism Summary"))
                },
                modifier = Modifier.testTag("share_summary_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Highlighted Matched Text Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("highlighted_content_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MATCHED CONTENT HIGHLIGHT",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MunasarBluePrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Sections identified with matching web sources are highlighted in red.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Build highlighted annotated string
                val rawText = uiState.inputText.ifEmpty {
                    if (result.matches.isNotEmpty()) result.matches.joinToString("\n\n") { it.text } else "No text"
                }

                val annotatedText = buildAnnotatedString {
                    var lastIndex = 0
                    val sortedMatches = result.matches.sortedBy { rawText.indexOf(it.text).takeIf { idx -> idx >= 0 } ?: Int.MAX_VALUE }

                    for (match in sortedMatches) {
                        val start = rawText.indexOf(match.text, lastIndex)
                        if (start >= 0) {
                            if (start > lastIndex) {
                                append(rawText.substring(lastIndex, start))
                            }
                            withStyle(
                                SpanStyle(
                                    background = Color(0xFFFEE2E2),
                                    color = PlagiarismAlertRed,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(match.text)
                            }
                            lastIndex = start + match.text.length
                        }
                    }
                    if (lastIndex < rawText.length) {
                        append(rawText.substring(lastIndex))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(14.dp)
                ) {
                    Text(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = MunasarNavyText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Individual Matching Passages Section
        Text(
            text = "MATCHING PASSAGES (${result.matches.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MunasarNavyText
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (result.matches.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = OriginalSuccessGreen,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "No duplicate passages found",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = OriginalSuccessGreen
                        )
                        Text(
                            text = "Content has passed verification with 100% originality.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                result.matches.forEachIndexed { index, match ->
                    MatchedPassageCard(
                        matchNumber = index + 1,
                        match = match
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
