package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MunasarBluePrimary
import com.example.ui.theme.OriginalSuccessGreen
import com.example.ui.theme.PlagiarismAlertRed

@Composable
fun SummaryStatsGrid(
    totalWords: Int,
    matchedWords: Int,
    sourcesCount: Int,
    originalWords: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("summary_stats_grid"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatMiniCard(
                modifier = Modifier.weight(1f),
                title = "Total Words",
                value = totalWords.toString(),
                accentColor = MaterialTheme.colorScheme.onSurface
            )
            StatMiniCard(
                modifier = Modifier.weight(1f),
                title = "Matched Words",
                value = matchedWords.toString(),
                accentColor = if (matchedWords > 0) PlagiarismAlertRed else MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatMiniCard(
                modifier = Modifier.weight(1f),
                title = "Sources Found",
                value = sourcesCount.toString(),
                accentColor = MunasarBluePrimary
            )
            StatMiniCard(
                modifier = Modifier.weight(1f),
                title = "Original Words",
                value = originalWords.toString(),
                accentColor = OriginalSuccessGreen
            )
        }
    }
}

@Composable
private fun StatMiniCard(
    title: String,
    value: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = accentColor
            )
        }
    }
}
