package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Clean Utility / Minimal Light Theme Palette for LankaBudget Pro (now dynamic vars!)
var SlateDark = Color(0xFFFBFDFD)         // Clean off-white background
var SurfaceDark = Color(0xFFFFFFFF)       // Crisp white cards
var SurfaceDarkSecondary = Color(0xFFEFF1F1) // Supporting elements background

// Material 3 Core Clean Teal Theme Color
val EmeraldIncome = Color(0xFF006A6A)     // Deep rich teal primary
var NavBarBackground = Color(0xFFEFF1F1)  // Clean navigation bar
var NavBarBorder = Color(0xFFDEE3E3)      // Bottom nav subtle top border
var HeaderPillBg = Color(0xFFCCE8E8)      // Light pastel teal header ring container

// 50/30/20 & Income Colors (Directly mapping from Clean HTML guidelines)
val ElectricNeeds = Color(0xFF006A6A)     // Needs (Teal, e.g., #006A6A)
val ElectricNeedsBg = Color(0xFFE0F2F2)   // Needs Background (#E0F2F2)
val ElectricNeedsBorder = Color(0xFFBFC9C8) // Needs Border (#BFC9C8)

val WarmWants = Color(0xFF6A6A00)         // Wants (Olive/Yellow-Green, e.g., #6A6A00)
val WarmWantsBg = Color(0xFFF2F1E0)       // Wants Background (#F2F1E0)
val WarmWantsBorder = Color(0xFFC9C8BF)   // Wants Border (#C9C8BF)

val SavingsIndigo = Color(0xFF6A0000)      // Savings (Rich Crimson-Red, e.g., #6A0000)
val SavingsIndigoBg = Color(0xFFF2E0E0)    // Savings Background (#F2E0E0)
val SavingsIndigoBorder = Color(0xFFC9BFBF) // Savings Border (#C9BFBF)

// Text and Accents (Strict matching of Clean Minimal design specs)
var TextPrimary = Color(0xFF191C1C)       // Very dark charcoal-teal
var TextSecondary = Color(0xFF3F4948)     // Subtle slate grey
val TextHighlight = Color(0xFF006A6A)     // Clean teal highlight
var GridDividerLabel = Color(0xFFDEE3E3)  // Clean light border grey
val DangerRed = Color(0xFFBA1A1A)         // Warning red/negative balance crimson
val SettlementSuccess = Color(0xFF006A6A) // Deep teal success highlight

// Dynamic Glass Backdrop color
var GlassCardContainerColor = Color.White.copy(alpha = 0.52f)
var GlassCardBorderColor = Color.White

/**
 * Configure global color palette variables dynamically to support Light, Dark, and Amoled themes.
 */
fun applyThemeColors(themeMode: String, isSystemDark: Boolean) {
    val activeDark = when (themeMode) {
        "Light" -> false
        "Dark", "Amoled" -> true
        else -> isSystemDark // "Auto" or fallback
    }

    if (themeMode == "Amoled") {
        // Pure black theme for AMOLED screens
        SlateDark = Color(0xFF000000)
        SurfaceDark = Color(0xFF0D0D0E)
        SurfaceDarkSecondary = Color(0xFF18181A)
        TextPrimary = Color(0xFFFFFFFF)
        TextSecondary = Color(0xFFCCCCCC)
        GridDividerLabel = Color(0xFF2E3232)
        NavBarBackground = Color(0xFF000000)
        NavBarBorder = Color(0xFF202022)
        HeaderPillBg = Color(0xFF003737)
        GlassCardContainerColor = Color.Black.copy(alpha = 0.65f)
        GlassCardBorderColor = Color(0xFF2E3232)
    } else if (activeDark) {
        // High-end dark theme
        SlateDark = Color(0xFF0F1113)
        SurfaceDark = Color(0xFF16191A)
        SurfaceDarkSecondary = Color(0xFF222627)
        TextPrimary = Color(0xFFE1E3E3)
        TextSecondary = Color(0xFFA1A5A5)
        GridDividerLabel = Color(0xFF3F4444)
        NavBarBackground = Color(0xFF16191A)
        NavBarBorder = Color(0xFF2E3232)
        HeaderPillBg = Color(0xFF003737)
        GlassCardContainerColor = Color(0xFF16191A).copy(alpha = 0.55f)
        GlassCardBorderColor = Color(0xFF3F4444)
    } else {
        // Standard elegant light theme
        SlateDark = Color(0xFFFBFDFD)
        SurfaceDark = Color(0xFFFFFFFF)
        SurfaceDarkSecondary = Color(0xFFEFF1F1)
        TextPrimary = Color(0xFF191C1C)
        TextSecondary = Color(0xFF3F4948)
        GridDividerLabel = Color(0xFFDEE3E3)
        NavBarBackground = Color(0xFFEFF1F1)
        NavBarBorder = Color(0xFFDEE3E3)
        HeaderPillBg = Color(0xFFCCE8E8)
        GlassCardContainerColor = Color.White.copy(alpha = 0.52f)
        GlassCardBorderColor = Color.White
    }
}
