package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    themeMode: String = "Auto",
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()
    
    // Apply dynamic colors to our global variables
    applyThemeColors(themeMode, isSystemDark)

    val activeColorScheme = if (themeMode == "Amoled" || themeMode == "Dark" || (themeMode == "Auto" && isSystemDark)) {
        darkColorScheme(
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
    } else {
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
    }

    MaterialTheme(
        colorScheme = activeColorScheme,
        typography = Typography,
        content = content
    )
}
