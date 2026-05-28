package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CleanColorScheme =
    lightColorScheme(
        primary = EmeraldIncome,
        secondary = ElectricNeeds,
        tertiary = WarmWants,
        background = SlateDark,
        surface = SurfaceDark,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
        surfaceVariant = SurfaceDarkSecondary,
        outline = GridDividerLabel
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Enforce our crisp Clean Minimal light style
    dynamicColor: Boolean = false, // Keep disabled to preserve custom palette
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CleanColorScheme,
        typography = Typography,
        content = content
    )
}
