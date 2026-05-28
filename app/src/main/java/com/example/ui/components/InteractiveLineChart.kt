package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.formatLKR
import com.example.ui.theme.*

@Composable
fun InteractiveLineChart(
    dataPoints: List<Pair<String, Double>>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(SurfaceDarkSecondary, RoundedCornerShape(16.dp)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "Not enough cashflow data points to render trajectory. Log some transactions!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val maxVal = dataPoints.maxOf { it.second }.coerceAtLeast(1.0)
    val minVal = dataPoints.minOf { it.second }.coerceAtMost(0.0)
    val valRange = (maxVal - minVal).coerceAtLeast(1.0)

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var touchX by remember { mutableStateOf<Float?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .border(1.dp, GridDividerLabel, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CASHFLOW BALANCE TRAJECTORY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = ElectricNeeds
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "A continuous scale of your net asset velocity",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // Show details of the selected point on-demand
            if (selectedIndex != null && selectedIndex!! < dataPoints.size) {
                val point = dataPoints[selectedIndex!!]
                Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                    Text(
                        text = formatLKR(point.second),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (point.second >= 0.0) SettlementSuccess else DangerRed
                    )
                    Text(
                        text = "Point: ${point.first}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            } else {
                Text(
                    text = "Drag/Tap to Inspect",
                    style = MaterialTheme.typography.labelSmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val width = size.width
                                val stepX = width.toFloat() / (dataPoints.size - 1)
                                val idx = (offset.x / stepX).coerceIn(0f, (dataPoints.size - 1).toFloat()).plus(0.5f).toInt()
                                selectedIndex = idx
                                touchX = idx * stepX
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                selectedIndex = null
                                touchX = null
                            },
                            onDragCancel = {
                                selectedIndex = null
                                touchX = null
                            },
                            onDrag = { change, dragAmount ->
                                val width = size.width
                                val stepX = width.toFloat() / (dataPoints.size - 1)
                                val currentTouchX = (touchX ?: change.position.x) + dragAmount.x
                                val idx = (currentTouchX / stepX).coerceIn(0f, (dataPoints.size - 1).toFloat()).plus(0.5f).toInt()
                                selectedIndex = idx
                                touchX = idx * stepX
                            }
                        )
                    }
            ) {
                val width = size.width
                val height = size.height

                // Draw horizontal guide grids
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    drawLine(
                        color = GridDividerLabel.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val points = mutableListOf<Offset>()
                val stepX = width / (dataPoints.size - 1)

                dataPoints.forEachIndexed { idx, pair ->
                    val x = idx * stepX
                    val yNormalized = ((pair.second - minVal) / valRange).toFloat()
                    val y = height - (yNormalized * height)
                    points.add(Offset(x, y))
                }

                // Create linear transition path
                val connectionPath = Path()
                points.forEachIndexed { index, offset ->
                    if (index == 0) {
                        connectionPath.moveTo(offset.x, offset.y)
                    } else {
                        connectionPath.lineTo(offset.x, offset.y)
                    }
                }

                // Draw filled gradient under the path
                val fillPath = Path().apply {
                    addPath(connectionPath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ElectricNeeds.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )

                // Draw main connection line
                drawPath(
                    path = connectionPath,
                    color = ElectricNeeds,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw circular markers
                points.forEachIndexed { index, offset ->
                    val isSelected = selectedIndex == index
                    drawCircle(
                        color = if (isSelected) ElectricNeeds else Color.White,
                        radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = ElectricNeeds,
                        radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                        center = offset,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Draw active crosshair vertical bar
                touchX?.let { tx ->
                    if (selectedIndex != null && selectedIndex!! < points.size) {
                        val activeY = points[selectedIndex!!].y
                        drawLine(
                            color = ElectricNeeds.copy(alpha = 0.6f),
                            start = Offset(tx, 0f),
                            end = Offset(tx, height),
                            strokeWidth = 1.5.dp.toPx()
                        )
                        drawCircle(
                            color = ElectricNeeds,
                            radius = 8.dp.toPx(),
                            center = Offset(tx, activeY)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = Offset(tx, activeY)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Render minimum, average, and maximum bounds
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min: ${formatLKR(minVal)}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "Average: ${formatLKR(dataPoints.map { it.second }.average())}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = "Max: ${formatLKR(maxVal)}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = ElectricNeeds
            )
        }
    }
}
