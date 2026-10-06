package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MunasarLightColorScheme = lightColorScheme(
  primary = MunasarBluePrimary,
  onPrimary = Color.White,
  primaryContainer = MunasarBlueContainer,
  onPrimaryContainer = MunasarOnBlueContainer,
  secondary = MunasarBlueLight,
  onSecondary = Color.White,
  background = MunasarBackground,
  onBackground = MunasarNavyText,
  surface = MunasarSurface,
  onSurface = MunasarNavyText,
  surfaceVariant = MunasarSurfaceVariant,
  onSurfaceVariant = MunasarSlateSubtext,
  outline = MunasarBorder,
  error = PlagiarismAlertRed,
  onError = Color.White,
  errorContainer = PlagiarismRedContainer,
  onErrorContainer = PlagiarismAlertRed,
)

private val MunasarDarkColorScheme = darkColorScheme(
  primary = MunasarBlueLight,
  onPrimary = Color(0xFF002266),
  primaryContainer = Color(0xFF0D3B9C),
  onPrimaryContainer = Color(0xFFD6E4FF),
  secondary = MunasarBluePrimary,
  onSecondary = Color.White,
  background = Color(0xFF0B132B),
  onBackground = Color(0xFFF1F5F9),
  surface = Color(0xFF131D3B),
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF1E293B),
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF334155),
  error = Color(0xFFF87171),
  onError = Color.Black,
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep Munasar brand identity consistent
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> MunasarDarkColorScheme
    else -> MunasarLightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
