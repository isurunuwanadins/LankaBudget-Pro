package com.example.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

/**
 * A beautiful, highly-polished 3D Liquid Glass Card modifier.
 * Simulates high-end liquid-lens elements from the reference video.
 * Applies custom specular light hit and refractive bounce highlights, and
 * dynamically adapts color blending based on active light/dark/amoled state.
 */
fun Modifier.liquidGlassCard(
    cornerRadius: Dp = 24.dp,
    containerColor: Color? = null,
    borderColor: Color? = null,
    hasShadow: Boolean = false
): Modifier {
    val shadowModifier = if (hasShadow) {
        this.drawBehind {
            val radiusPx = cornerRadius.toPx()
            // High fidelity wide physical drop shadows that go outside clipping bounds
            // Primary tight shadow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.28f),
                topLeft = Offset(0f, 6.dp.toPx()),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
            )
            // Wide atmospheric soft ambient shadow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.16f),
                topLeft = Offset(0f, 12.dp.toPx()),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
            )
        }
    } else {
        this
    }

    return shadowModifier.drawBehind {
        // Evaluate theme context based on global SlateDark variables
        val isSystemDark = SlateDark != Color(0xFFFBFDFD)
        val isAmoled = SlateDark == Color(0xFF000000)

        // Resolve container background color
        val baseColor = containerColor ?: GlassCardContainerColor
        val resolvedBg = if (isSystemDark) {
            // Translate light colors to gorgeous dark amoled overlays dynamically
            if (baseColor.red > 0.8f && baseColor.green > 0.8f && baseColor.blue > 0.8f) {
                if (isAmoled) {
                    Color.Black.copy(alpha = baseColor.alpha * 0.85f)
                } else {
                    Color(0xFF16191A).copy(alpha = baseColor.alpha * 0.95f)
                }
            } else {
                baseColor
            }
        } else {
            baseColor
        }

        // Draw translucent body fill
        val radiusPx = cornerRadius.toPx()
        drawRoundRect(
            color = resolvedBg,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx)
        )

        // 1. Sleek 3D upper-left inner highlight representing direct physical light hit
        val glossBrush = Brush.linearGradient(
            colors = if (isSystemDark) {
                listOf(
                    Color.White.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.12f),
                    Color.Transparent,
                    Color.Transparent
                )
            } else {
                listOf(
                    Color.White.copy(alpha = 0.8f),
                    Color.White.copy(alpha = 0.3f),
                    Color.Transparent,
                    Color.Transparent
                )
            },
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.4f, size.height * 0.4f)
        )
        drawRoundRect(
            brush = glossBrush,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = 2.2f)
        )

        // 2. Secondary soft refractive mercury-glow bounce highlight at the bottom right
        val bounceBrush = Brush.linearGradient(
            colors = if (isSystemDark) {
                listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color.White.copy(alpha = 0.03f),
                    Color.White.copy(alpha = 0.15f)
                )
            } else {
                listOf(
                    Color.Transparent,
                    Color.Transparent,
                    Color.White.copy(alpha = 0.05f),
                    Color.White.copy(alpha = 0.28f)
                )
            },
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
        drawRoundRect(
            brush = bounceBrush,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx, radiusPx),
            style = Stroke(width = 1.2f)
        )
    }
    .clip(RoundedCornerShape(cornerRadius))
    .border(
        width = 1.0.dp,
        brush = Brush.linearGradient(
            colors = let {
                val isSystemDark = SlateDark != Color(0xFFFBFDFD)
                val isAmoled = SlateDark == Color(0xFF000000)
                val baseBorder = borderColor ?: GlassCardBorderColor
                val resolvedBorder = if (isSystemDark) {
                    if (baseBorder.red > 0.8f && baseBorder.green > 0.8f && baseBorder.blue > 0.8f) {
                        if (isAmoled) Color(0xFF2E3232) else Color(0xFF3F4444)
                    } else {
                        baseBorder
                    }
                } else {
                    baseBorder
                }

                listOf(
                    resolvedBorder.copy(alpha = 0.7f),
                    resolvedBorder.copy(alpha = 0.15f),
                    resolvedBorder.copy(alpha = 0.05f),
                    resolvedBorder.copy(alpha = 0.45f)
                )
            },
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
        modifier = modifier.fillMaxSize()
    ) {
        // Blur-filtered background gradient layer to perfectly replicate CSS backdrop-filter: blur(15px)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .let { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) it.blur(15.dp) else it }
                .drawBehind {
                    // Determine layout mode based on SlateDark color variable
                    val isAmoled = SlateDark == Color(0xFF000000)
                    val isDark = SlateDark == Color(0xFF0F1113)

                    if (isAmoled) {
                        // Pure solid pitch black amoled background for maximum battery efficiency
                        drawRect(color = Color(0xFF000000))

                        // Breathtaking cyber-obsidian depth blooms on screen edges
                        val amoledGlow = Brush.radialGradient(
                            colors = listOf(
                                Color(0x3B00E5FF), // enhanced glow for blur layer
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.85f, size.height * 0.8f),
                            radius = size.width * 0.65f
                        )
                        drawRect(brush = amoledGlow)

                        val amoledEmerald = Brush.radialGradient(
                            colors = listOf(
                                Color(0x3400E676), // enhanced emerald bloom
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.2f),
                            radius = size.width * 0.55f
                        )
                        drawRect(brush = amoledEmerald)
                    } else if (isDark) {
                        // Modern premium dark glass-morphic theme
                        val verticalBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0C0E10), // Top: Deep dark charcoal
                                Color(0xFF121417), // Mid: Slate black
                                Color(0xFF1B1E22)  // Bottom: Dark sapphire twilight
                            )
                        )
                        drawRect(brush = verticalBrush)

                        // Soft glowing neon blobs for dark visual texture
                        val tealBrush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x4200838F), // Indigo/teal glow (enhanced opacity for backdrop-blur depth)
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.7f, size.height * 0.2f),
                            radius = size.width * 0.6f
                        )
                        drawRect(brush = tealBrush)

                        val lavenderBrush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x337E57C2), // Purple/lavender glow
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.2f, size.height * 0.6f),
                            radius = size.width * 0.8f
                        )
                        drawRect(brush = lavenderBrush)

                        val coralBrush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x28ED4F28), // Sunset coral glow
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.8f, size.height * 0.7f),
                            radius = size.width * 0.7f
                        )
                        drawRect(brush = coralBrush)
                    } else {
                        // Classic elegant light theme
                        val verticalBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE5F7F6), // Top: Liquid light mint teal
                                Color(0xFFEDF2FB), // Mid: Water blue
                                Color(0xFFF3E7F3)  // Bottom: Smooth coral-pink / purple
                            )
                        )
                        drawRect(brush = verticalBrush)

                        val tealBrush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x5E00838F), // Glowing Teal
                                Color.Transparent
                            ),
                            center = Offset(600f, 400f),
                            radius = 450f
                        )
                        drawRect(brush = tealBrush)

                        val lavenderBrush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x4F7E57C2), // Soft Lavender
                                Color.Transparent
                            ),
                            center = Offset(200f, 1000f),
                            radius = 550f
                        )
                        drawRect(brush = lavenderBrush)

                        val coralBrush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x47FF5722), // Soft Coral / Orange
                                Color.Transparent
                            ),
                            center = Offset(1000f, 400f),
                            radius = 500f
                        )
                        drawRect(brush = coralBrush)

                        val citronBrush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x40FEB709), // Warm Citron/Golden
                                Color.Transparent
                            ),
                            center = Offset(200f, 1300f),
                            radius = 650f
                        )
                        drawRect(brush = citronBrush)
                    }
                }
        )
        content()
    }
}

/**
 * A beautiful, tactile Liquid Glass Toggle Switch.
 * Mimics organic fluid mercury/liquid stretching and sliding transition effects
 * dynamically mapped to movement speeds.
 */
@Composable
fun LiquidGlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSystemDark = SlateDark != Color(0xFFFBFDFD)
    val isAmoled = SlateDark == Color(0xFF000000)

    val transition = updateTransition(targetState = checked, label = "LiquidToggle")
    val thumbOffsetPercent by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow) },
        label = "ThumbOffset"
    ) { state -> if (state) 1f else 0f }

    // Organic speed morphing factor
    val speedFactor = 1f - kotlin.math.abs(thumbOffsetPercent - 0.5f) * 2f // peaks at 1.0 in transition

    Box(
        modifier = modifier
            .width(55.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(
                if (checked) {
                    if (isSystemDark) EmeraldIncome.copy(alpha = 0.2f) else EmeraldIncome.copy(alpha = 0.15f)
                } else {
                    if (isSystemDark) Color.White.copy(alpha = 0.05f) else SurfaceDarkSecondary.copy(alpha = 0.5f)
                }
            )
            .border(
                1.2.dp,
                Brush.verticalGradient(
                    colors = if (isSystemDark) {
                        listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.02f))
                    } else {
                        listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.1f))
                    }
                ),
                CircleShape
            )
            .clickable { onCheckedChange(!checked) }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxMoveWidth = maxWidth - 24.dp
            val xOffset = maxMoveWidth * thumbOffsetPercent
            
            // Stretches length organic bubble, shrinks on ends
            val currentThumbWidth = 24.dp + (8.dp * speedFactor)
            
            Box(
                modifier = Modifier
                    .offset(x = xOffset - (4.dp * speedFactor))
                    .width(currentThumbWidth)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (checked) {
                                listOf(EmeraldIncome, Color(0xFF26A69A))
                            } else {
                                if (isSystemDark) {
                                    listOf(Color(0xFF505A5F), Color(0xFF707A7F))
                                } else {
                                    listOf(Color(0xFF90A4AE), Color(0xFFB0BEC5))
                                }
                            }
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
                    .drawBehind {
                        // Gloss lens shine inside the slider ball
                        val shineHeight = size.height * 0.4f
                        val shineBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.75f),
                                Color.Transparent
                            )
                        )
                        drawRoundRect(
                            brush = shineBrush,
                            size = Size(size.width, shineHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f, size.height / 2f)
                        )
                    }
            )
        }
    }
}
