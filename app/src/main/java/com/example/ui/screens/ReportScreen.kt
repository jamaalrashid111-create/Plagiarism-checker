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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.NavigationTab
import com.example.ui.components.MatchedPassageCard
import com.example.ui.components.SourceCard
import com.example.ui.theme.MunasarBlueContainer
import com.example.ui.theme.MunasarBluePrimary
import com.example.ui.theme.MunasarNavyText
import com.example.ui.theme.OriginalSuccessGreen
import com.example.ui.theme.PlagiarismAlertRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (uiState.showSourcesScreen) {
        SourcesScreen(uiState = uiState, viewModel = viewModel, modifier = modifier)
        return
    }

    val result = uiState.currentResult
    if (result == null) {
        ResultsScreen(uiState = uiState, viewModel = viewModel, modifier = modifier)
        return
    }

    val scrollState = rememberScrollState()
    val totalWords = result.wordCount.takeIf { it > 0 } ?: uiState.documentStats.wordCount
    val originalWords = (totalWords - result.matchedWordCount).coerceAtLeast(0)
    val formattedDate = SimpleDateFormat("MMMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("report_screen")
    ) {
        // Report Document Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header Banner inside Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Munasar Plagiarism Checker",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MunasarBluePrimary
                        )
                        Text(
                            text = "Official Authenticity Report",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MunasarBlueContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "VERIFIED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MunasarBluePrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(14.dp))

                // Metadata Rows
                ReportMetaRow(label = "Date", value = formattedDate)
                ReportMetaRow(label = "Document Name", value = uiState.currentDocumentName)
                ReportMetaRow(label = "Word Count", value = "$totalWords words")
                ReportMetaRow(label = "Matched Words", value = "${result.matchedWordCount} words")
                ReportMetaRow(label = "Sources Found", value = "${result.sources.size} sources")

                Spacer(modifier = Modifier.height(16.dp))

                // Score Highlights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Plagiarism Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${result.plagiarismPercentage}%",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = PlagiarismAlertRed
                            )
                            Text(
                                text = "Plagiarism",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PlagiarismAlertRed
                            )
                        }
                    }

                    // Originality Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${result.originalPercentage}%",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = OriginalSuccessGreen
                            )
                            Text(
                                text = "Original Content",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OriginalSuccessGreen
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: [Export PDF] and [Share Report]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    viewModel.exportPdfReport(context) { file, uri ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("report_export_pdf_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MunasarBluePrimary)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export PDF", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    viewModel.exportPdfReport(context) { file, uri ->
                        try {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                putExtra(Intent.EXTRA_SUBJECT, "Munasar Plagiarism Report - ${uiState.currentDocumentName}")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Plagiarism PDF Report"))
                        } catch (_: Exception) {}
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("report_share_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Report", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Detailed Source List
        Text(
            text = "DETAILED SOURCE LIST (${result.sources.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MunasarNavyText
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (result.sources.isEmpty()) {
            Text(
                text = "No matching external sources found in the plagiarism database.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                result.sources.forEach { source ->
                    SourceCard(source = source)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Detailed Matching Passages
        Text(
            text = "DETAILED MATCHING PASSAGES (${result.matches.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MunasarNavyText
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (result.matches.isEmpty()) {
            Text(
                text = "No direct matching phrases detected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                result.matches.forEachIndexed { index, match ->
                    MatchedPassageCard(matchNumber = index + 1, match = match)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ReportMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MunasarNavyText
        )
    }
}
