package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A beautiful, highly-polished Liquid Glass Card modifier.
 * Simulates a high-end translucent glass panel with glowing glossy boundaries.
 */
fun Modifier.liquidGlassCard(
    cornerRadius: Dp = 24.dp,
    containerColor: Color = Color.White.copy(alpha = 0.58f),
    borderColor: Color = Color.White
): Modifier {
    return this
        .clip(RoundedCornerShape(cornerRadius))
        .background(containerColor)
        .border(
            width = 1.2.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    borderColor.copy(alpha = 0.85f),
                    borderColor.copy(alpha = 0.15f),
                    borderColor.copy(alpha = 0.05f),
                    borderColor.copy(alpha = 0.5f)
                ),
                start = Offset.Zero,
                end = Offset.Infinite
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
}

/**
 * A background component that renders glowing, gently swaying liquid gradient-blobs
 * behind its contents, creating true "liquid glassmorphism" aesthetics.
 */
@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // Drawing inside drawBehind avoids recomposition loops of the content view
                // 1. Vertical base background gradient
                val verticalBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE5F7F6), // Top: Liquid light mint teal
                        Color(0xFFEDF2FB), // Mid: Water blue
                        Color(0xFFF3E7F3)  // Bottom: Smooth coral-pink / purple
                    )
                )
                drawRect(brush = verticalBrush)

                // 2. Glowing Teal Blob (Static position)
                val tealBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3300838F), // Glowing Teal
                        Color.Transparent
                    ),
                    center = Offset(600f, 400f),
                    radius = 450f
                )
                drawRect(brush = tealBrush)

                // 3. Soft Lavender Blob (Static position)
                val lavenderBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x247E57C2), // Soft Lavender
                        Color.Transparent
                    ),
                    center = Offset(200f, 1000f),
                    radius = 550f
                )
                drawRect(brush = lavenderBrush)

                // 4. Soft Coral Blob (Static position)
                val coralBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x22FF5722), // Soft Coral / Orange
                        Color.Transparent
                    ),
                    center = Offset(1000f, 400f),
                    radius = 500f
                )
                drawRect(brush = coralBrush)

                // 5. Warm Citron Blob (Static position)
                val citronBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x1DFEB709), // Warm Citron/Golden
                        Color.Transparent
                    ),
                    center = Offset(200f, 1300f),
                    radius = 650f
                )
                drawRect(brush = citronBrush)
            }
    ) {
        content()
    }
}
