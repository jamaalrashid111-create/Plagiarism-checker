package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OriginalGreenContainer
import com.example.ui.theme.OriginalSuccessGreen
import com.example.ui.theme.PlagiarismAlertRed
import com.example.ui.theme.PlagiarismRedContainer

@Composable
fun ScoreCircularBadge(
    plagiarismPercentage: Int,
    originalPercentage: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("score_circular_badge"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular Progress Canvas
                Canvas(modifier = Modifier.size(175.dp)) {
                    val strokeWidthPx = 16.dp.toPx()
                    // Track background
                    drawCircle(
                        color = Color(0xFFE2E8F0),
                        style = Stroke(width = strokeWidthPx)
                    )

                    // Original portion (Green)
                    drawArc(
                        color = Color(0xFF16A34A),
                        startAngle = -90f,
                        sweepAngle = (originalPercentage / 100f) * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    // Plagiarism portion (Red)
                    if (plagiarismPercentage > 0) {
                        drawArc(
                            color = Color(0xFFDC2626),
                            startAngle = -90f + ((originalPercentage / 100f) * 360f),
                            sweepAngle = (plagiarismPercentage / 100f) * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center Text Content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$plagiarismPercentage%",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = if (plagiarismPercentage > 0) PlagiarismAlertRed else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Plagiarism",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (plagiarismPercentage > 0) PlagiarismAlertRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$originalPercentage% Original",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = OriginalSuccessGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Indicator Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // Plagiarism indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PlagiarismRedContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PlagiarismAlertRed)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$plagiarismPercentage% Plagiarism",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = PlagiarismAlertRed
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Originality indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(OriginalGreenContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(OriginalSuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$originalPercentage% Original",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OriginalSuccessGreen
                    )
                }
            }
        }
    }
}
