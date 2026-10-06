package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PlanType
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.theme.MunasarBlueContainer
import com.example.ui.theme.MunasarBluePrimary
import com.example.ui.theme.MunasarNavyText
import com.example.ui.theme.OriginalSuccessGreen
import com.example.ui.theme.PlagiarismAlertRed

@Composable
fun SettingsScreen(
    uiState: MainUiState,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showApiDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "Settings & Configuration",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MunasarNavyText
        )
        Text(
            text = "Manage account, backend API connection, and preferences",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Account Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAuthDialog = true }
                .testTag("account_profile_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MunasarBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = MunasarBluePrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.userProfile.isLoggedIn) uiState.userProfile.displayName else "Guest Mode",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MunasarNavyText
                    )
                    Text(
                        text = if (uiState.userProfile.isLoggedIn) (uiState.userProfile.email ?: "") else "Tap to sign in or register",
                        style = MaterialTheme.typography.bodySmall,
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
                        text = uiState.userProfile.plan.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MunasarBluePrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Backend API Configuration Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showApiDialog = true }
                .testTag("api_configuration_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isApiConfigured) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Api,
                    contentDescription = null,
                    tint = if (uiState.isApiConfigured) OriginalSuccessGreen else Color(0xFFD97706),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Plagiarism API Backend",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MunasarNavyText
                    )
                    Text(
                        text = if (uiState.isApiConfigured) "Service Connected" else "Service is not configured yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (uiState.isApiConfigured) OriginalSuccessGreen else Color(0xFFB45309),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Settings Section: Subscriptions & Ads
        SettingsSectionHeader(title = "MEMBERSHIP & MONETIZATION")

        SettingsTile(
            icon = Icons.Default.Star,
            title = "Subscription Plan",
            subtitle = "${uiState.userProfile.plan.displayName} • ${uiState.userProfile.plan.wordLimit} words / scan",
            onClick = { showSubscriptionDialog = true }
        )

        SettingsTile(
            icon = Icons.Default.Campaign,
            title = "AdMob Integration",
            subtitle = "Production Architecture Ready (No fake ads)",
            onClick = {}
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Settings Section: Preferences
        SettingsSectionHeader(title = "PREFERENCES & NOTIFICATIONS")

        SettingsTile(
            icon = Icons.Default.Language,
            title = "Language",
            subtitle = "English (Default)",
            onClick = {}
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = MunasarBluePrimary)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text("Notifications", style = MaterialTheme.typography.bodyLarge, color = MunasarNavyText)
                    Text("Scan completion alerts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { notificationsEnabled = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MunasarBluePrimary)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Settings Section: Privacy & Data
        SettingsSectionHeader(title = "PRIVACY & SECURITY")

        SettingsTile(
            icon = Icons.Default.Security,
            title = "Privacy Policy",
            subtitle = "Zero document retention policy",
            onClick = { showPrivacyDialog = true }
        )

        SettingsTile(
            icon = Icons.Default.Description,
            title = "Terms of Service",
            subtitle = "Fair use and document analysis terms",
            onClick = { showTermsDialog = true }
        )

        SettingsTile(
            icon = Icons.Default.ClearAll,
            title = "Clear Local Data & History",
            subtitle = "Erase all saved reports on this device",
            textColor = PlagiarismAlertRed,
            onClick = { showClearDataDialog = true }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Settings Section: About Munasar
        SettingsSectionHeader(title = "ABOUT MUNASAR ONLINE")

        SettingsTile(
            icon = Icons.Default.Info,
            title = "About Munasar Plagiarism Checker",
            subtitle = "Version 1.0.0 (Production Architecture)",
            onClick = {}
        )

        SettingsTile(
            icon = Icons.Default.Public,
            title = "Official Website",
            subtitle = "https://www.munasar.online/",
            onClick = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.munasar.online/"))
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        )

        Spacer(modifier = Modifier.height(30.dp))
    }

    // API Configuration Dialog
    if (showApiDialog) {
        ApiConfigurationDialog(
            currentStatus = uiState.apiStatusMessage,
            isConfigured = uiState.isApiConfigured,
            onSaveGemini = { geminiKey ->
                viewModel.saveGeminiApiKey(geminiKey)
                showApiDialog = false
            },
            onSaveCustom = { url, key ->
                viewModel.saveCustomApiConfig(url, key)
                showApiDialog = false
            },
            onReset = {
                viewModel.clearCustomApiConfig()
                showApiDialog = false
            },
            onDismiss = { showApiDialog = false }
        )
    }

    // Account / Auth Dialog
    if (showAuthDialog) {
        AuthDialog(
            isLoggedIn = uiState.userProfile.isLoggedIn,
            email = uiState.userProfile.email,
            name = uiState.userProfile.displayName,
            onLogin = { email, name ->
                viewModel.loginUser(email, name)
                showAuthDialog = false
            },
            onLogout = {
                viewModel.logoutUser()
                showAuthDialog = false
            },
            onDismiss = { showAuthDialog = false }
        )
    }

    // Subscription Dialog
    if (showSubscriptionDialog) {
        SubscriptionDialog(
            currentPlan = uiState.userProfile.plan,
            onSelectPlan = { plan ->
                viewModel.upgradePlan(plan)
                showSubscriptionDialog = false
            },
            onDismiss = { showSubscriptionDialog = false }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Munasar Plagiarism Checker respects your privacy. Documents submitted for plagiarism checks are transmitted only to the configured plagiarism detection API. We do not store, index, sell, or disclose your private documents. Local history is stored strictly on your device and can be erased at any time."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Terms Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "By using Munasar Plagiarism Checker, you agree to submit only content you are authorized to analyze. Plagiarism scores reflect similarity against indexed online publications and do not constitute legal advice."
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear Data Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will permanently delete all scan records and cached reports from this device. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PlagiarismAlertRed)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        color = MunasarBluePrimary,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}

@Composable
private fun SettingsTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color = MunasarNavyText,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MunasarBluePrimary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = textColor, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ApiConfigurationDialog(
    currentStatus: String,
    isConfigured: Boolean,
    onSaveGemini: (String) -> Unit,
    onSaveCustom: (String, String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedProvider by remember { mutableStateOf(0) } // 0 = Gemini 2.5 Flash, 1 = Custom Proxy
    var geminiKeyInput by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf("") }
    var keyInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Plagiarism API Connection", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Status: $currentStatus",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isConfigured) OriginalSuccessGreen else PlagiarismAlertRed,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Provider Switcher Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedProvider == 0) MunasarBluePrimary else Color.Transparent)
                            .clickable { selectedProvider = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gemini AI",
                            color = if (selectedProvider == 0) Color.White else MunasarNavyText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedProvider == 1) MunasarBluePrimary else Color.Transparent)
                            .clickable { selectedProvider = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Custom Proxy",
                            color = if (selectedProvider == 1) Color.White else MunasarNavyText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedProvider == 0) {
                    Text(
                        text = "Connects directly to the Gemini 2.5 Flash analysis engine:\nhttps://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = geminiKeyInput,
                        onValueChange = { geminiKeyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Connect your custom plagiarism detection proxy endpoint:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Backend URL (e.g. https://api.munasar.online)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("API Key / Bearer Token") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedProvider == 0) {
                        onSaveGemini(geminiKeyInput)
                    } else {
                        onSaveCustom(urlInput, keyInput)
                    }
                },
                enabled = (selectedProvider == 0 && geminiKeyInput.isNotBlank()) || (selectedProvider == 1 && urlInput.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = MunasarBluePrimary)
            ) {
                Text("Save & Connect")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) {
                    Text("Reset")
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
private fun AuthDialog(
    isLoggedIn: Boolean,
    email: String?,
    name: String,
    onLogin: (String, String) -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    var emailInput by remember { mutableStateOf(email ?: "") }
    var nameInput by remember { mutableStateOf(name.takeIf { it != "Guest User" } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isLoggedIn) "User Account" else "Sign In / Register", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (isLoggedIn) {
                    Text("You are logged in as:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(name, fontWeight = FontWeight.Bold)
                    Text(email ?: "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Enter your email to sign in or create an account for history syncing and higher limits:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (isLoggedIn) {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = PlagiarismAlertRed)
                ) {
                    Text("Sign Out")
                }
            } else {
                Button(
                    onClick = {
                        val finalName = nameInput.ifBlank { "User" }
                        onLogin(emailInput, finalName)
                    },
                    enabled = emailInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MunasarBluePrimary)
                ) {
                    Text("Sign In")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun SubscriptionDialog(
    currentPlan: PlanType,
    onSelectPlan: (PlanType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Subscription Plans", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PlanOptionCard(
                    title = "Free Plan",
                    subtitle = "1,500 words per check • 5 checks / day",
                    isSelected = currentPlan == PlanType.FREE,
                    onSelect = { onSelectPlan(PlanType.FREE) }
                )
                PlanOptionCard(
                    title = "Munasar Pro",
                    subtitle = "25,000 words per check • 100 checks / day • PDF export",
                    isSelected = currentPlan == PlanType.PRO,
                    onSelect = { onSelectPlan(PlanType.PRO) }
                )
                Text(
                    text = "Prepared for Google Play Billing subscription integration.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun PlanOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MunasarBlueContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MunasarBluePrimary) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = MunasarNavyText)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Active", tint = MunasarBluePrimary)
            }
        }
    }
}
