package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun JointBudgetConcentricRings(
    needsProgress: Float,
    wantsProgress: Float,
    savingsProgress: Float,
    isNeedsWarning: Boolean = false,
    isWantsWarning: Boolean = false,
    isSavingsWarning: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Elegant pulsing animation for warnings (glowing crimson red transition)
    val infiniteTransition = rememberInfiniteTransition(label = "WarningGlow")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AlphaPulse"
    )

    val needsColor = if (isNeedsWarning) DangerRed else ElectricNeeds
    val wantsColor = if (isWantsWarning) DangerRed else WarmWants
    val savingsColor = if (isSavingsWarning) DangerRed else SavingsIndigo

    // Standard ring styling with premium strokes
    val strokeWidthDp = 10.dp
    val spacingDp = 12.dp
    
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidthDp.toPx() }
    val spacingPx = with(density) { spacingDp.toPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val canvasSize = size
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            
            // Outer Ring (Needs: 50%)
            val radiusOuter = (canvasSize.width.coerceAtMost(canvasSize.height) / 2f) - (strokeWidthPx / 2f)
            drawCircle(
                color = ElectricNeedsBg,
                radius = radiusOuter,
                center = center,
                style = Stroke(width = strokeWidthPx)
            )
            // Extra glowing ring if warning active
            if (isNeedsWarning) {
                drawCircle(
                    color = DangerRed.copy(alpha = 0.15f * pulseAlpha),
                    radius = radiusOuter + strokeWidthPx / 2f,
                    center = center,
                    style = Stroke(width = strokeWidthPx * 1.8f)
                )
            }
            drawArc(
                color = if (isNeedsWarning) needsColor.copy(alpha = pulseAlpha) else needsColor,
                startAngle = -90f,
                sweepAngle = 360f * needsProgress,
                useCenter = false,
                topLeft = Offset(center.x - radiusOuter, center.y - radiusOuter),
                size = Size(radiusOuter * 2, radiusOuter * 2),
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
 
            // Middle Ring (Wants: 30%)
            val radiusMiddle = radiusOuter - strokeWidthPx - spacingPx
            if (radiusMiddle > 0) {
                drawCircle(
                    color = WarmWantsBg,
                    radius = radiusMiddle,
                    center = center,
                    style = Stroke(width = strokeWidthPx)
                )
                // Extra glowing ring if warning active
                if (isWantsWarning) {
                    drawCircle(
                        color = DangerRed.copy(alpha = 0.15f * pulseAlpha),
                        radius = radiusMiddle + strokeWidthPx / 2f,
                        center = center,
                        style = Stroke(width = strokeWidthPx * 1.8f)
                    )
                }
                drawArc(
                    color = if (isWantsWarning) wantsColor.copy(alpha = pulseAlpha) else wantsColor,
                    startAngle = -90f,
                    sweepAngle = 360f * wantsProgress,
                    useCenter = false,
                    topLeft = Offset(center.x - radiusMiddle, center.y - radiusMiddle),
                    size = Size(radiusMiddle * 2, radiusMiddle * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
 
            // Inner Ring (Savings: 20%)
            val radiusInner = radiusMiddle - strokeWidthPx - spacingPx
            if (radiusInner > 0) {
                drawCircle(
                    color = SavingsIndigoBg,
                    radius = radiusInner,
                    center = center,
                    style = Stroke(width = strokeWidthPx)
                )
                // Extra glowing ring if warning active
                if (isSavingsWarning) {
                    drawCircle(
                        color = DangerRed.copy(alpha = 0.15f * pulseAlpha),
                        radius = radiusInner + strokeWidthPx / 2f,
                        center = center,
                        style = Stroke(width = strokeWidthPx * 1.8f)
                    )
                }
                drawArc(
                    color = if (isSavingsWarning) savingsColor.copy(alpha = pulseAlpha) else savingsColor,
                    startAngle = -90f,
                    sweepAngle = 360f * savingsProgress,
                    useCenter = false,
                    topLeft = Offset(center.x - radiusInner, center.y - radiusInner),
                    size = Size(radiusInner * 2, radiusInner * 2),
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }
 
        // Concentric central matrix label
        Text(
            text = "50/30/20",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            ),
            color = if (isNeedsWarning || isWantsWarning || isSavingsWarning) DangerRed else TextSecondary
        )
    }
}
