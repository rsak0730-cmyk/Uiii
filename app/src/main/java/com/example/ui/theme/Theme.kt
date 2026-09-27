package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.NeonTheme

@Composable
fun MyApplicationTheme(
    neonTheme: NeonTheme = NeonTheme.NEON_BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        primary = neonTheme.primary,
        onPrimary = Color.Black,
        primaryContainer = neonTheme.bgGlow,
        onPrimaryContainer = Color.White,
        secondary = neonTheme.accent,
        onSecondary = Color.Black,
        background = Color(0xFF090A10),
        onBackground = Color.White,
        surface = Color(0xFF10131B),
        onSurface = Color.White,
        surfaceContainer = Color(0xFF141722),
        outline = Color(0xFF262B3A)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
