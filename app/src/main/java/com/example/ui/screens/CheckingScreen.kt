package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckingStep
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.MunasarBlueContainer
import com.example.ui.theme.MunasarBluePrimary
import com.example.ui.theme.MunasarNavyText

@Composable
fun CheckingScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(24.dp)
            .testTag("checking_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(100.dp),
                        color = MunasarBluePrimary,
                        strokeWidth = 6.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MunasarBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MunasarBluePrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                Text(
                    text = "Munasar Plagiarism Engine",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MunasarNavyText
                )

                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = uiState.checkingStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "checking_status"
                ) { step ->
                    Text(
                        text = step.label,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                        color = MunasarBluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Progress Step Checklist
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StepItem(
                        label = "Analyzing your content...",
                        isDone = uiState.checkingStep != CheckingStep.ANALYZING,
                        isActive = uiState.checkingStep == CheckingStep.ANALYZING
                    )
                    StepItem(
                        label = "Searching for matching content...",
                        isDone = uiState.checkingStep == CheckingStep.COMPARING,
                        isActive = uiState.checkingStep == CheckingStep.SEARCHING
                    )
                    StepItem(
                        label = "Comparing sources...",
                        isDone = false,
                        isActive = uiState.checkingStep == CheckingStep.COMPARING
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { viewModel.cancelCheck() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("cancel_check_button")
                ) {
                    Text("Cancel Check")
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    label: String,
    isDone: Boolean,
    isActive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isDone) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(18.dp)
            )
        } else if (isActive) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MunasarBluePrimary,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.HourglassTop,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isActive) MunasarBluePrimary else if (isDone) Color(0xFF16A34A) else Color(0xFF64748B),
            fontWeight = if (isActive || isDone) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
