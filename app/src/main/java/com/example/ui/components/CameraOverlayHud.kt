package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GridType
import com.example.model.NormalizedRect
import com.example.util.OrientationData
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CameraOverlayHud(
    modifier: Modifier = Modifier,
    gridType: GridType,
    isLevelerEnabled: Boolean,
    orientation: OrientationData,
    targetBox: NormalizedRect?
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Grid & Target Box Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 1. Draw Rule of Thirds
            if (gridType == GridType.RULE_OF_THIRDS) {
                val gridColor = Color.White.copy(alpha = 0.35f)
                val dotColor = Color(0xFFFFD54F).copy(alpha = 0.7f)
                val strokeWidth = 1.5.dp.toPx()

                val col1 = canvasWidth / 3f
                val col2 = (canvasWidth / 3f) * 2f
                val row1 = canvasHeight / 3f
                val row2 = (canvasHeight / 3f) * 2f

                // Vertical lines
                drawLine(gridColor, Offset(col1, 0f), Offset(col1, canvasHeight), strokeWidth)
                drawLine(gridColor, Offset(col2, 0f), Offset(col2, canvasHeight), strokeWidth)

                // Horizontal lines
                drawLine(gridColor, Offset(0f, row1), Offset(canvasWidth, row1), strokeWidth)
                drawLine(gridColor, Offset(0f, row2), Offset(canvasWidth, row2), strokeWidth)

                // 4 Power Points (Intersections)
                val powerPoints = listOf(
                    Offset(col1, row1),
                    Offset(col2, row1),
                    Offset(col1, row2),
                    Offset(col2, row2)
                )
                powerPoints.forEach { pt ->
                    drawCircle(dotColor, radius = 4.dp.toPx(), center = pt)
                    drawCircle(Color.Black.copy(alpha = 0.5f), radius = 2.dp.toPx(), center = pt)
                }
            } else if (gridType == GridType.GOLDEN_SPIRAL) {
                // Golden ratio Fibonacci spiral approximation
                val spiralColor = Color(0xFFFFB74D).copy(alpha = 0.45f)
                val strokeWidth = 2.dp.toPx()

                val path = Path().apply {
                    moveTo(0f, canvasHeight)
                    cubicTo(
                        canvasWidth * 0.618f, canvasHeight,
                        canvasWidth, canvasHeight * 0.618f,
                        canvasWidth, 0f
                    )
                    cubicTo(
                        canvasWidth, canvasHeight * 0.382f,
                        canvasWidth * 0.382f, 0f,
                        canvasWidth * 0.382f, canvasHeight * 0.382f
                    )
                    cubicTo(
                        canvasWidth * 0.382f, canvasHeight * 0.618f,
                        canvasWidth * 0.618f, canvasHeight * 0.618f,
                        canvasWidth * 0.618f, canvasHeight * 0.382f
                    )
                }
                drawPath(path, spiralColor, style = Stroke(strokeWidth))
            }

            // 2. Draw AI Target Bounding Box
            targetBox?.let { box ->
                val left = box.xmin * canvasWidth
                val top = box.ymin * canvasHeight
                val right = box.xmax * canvasWidth
                val bottom = box.ymax * canvasHeight
                val boxWidth = right - left
                val boxHeight = bottom - top

                val targetColor = Color(0xFF10B981).copy(alpha = pulseAlpha)
                val cornerLength = (boxWidth * 0.18f).coerceAtLeast(18.dp.toPx())
                val cornerStroke = 3.dp.toPx()

                // Corner brackets
                // Top-Left
                drawLine(targetColor, Offset(left, top), Offset(left + cornerLength, top), cornerStroke)
                drawLine(targetColor, Offset(left, top), Offset(left, top + cornerLength), cornerStroke)

                // Top-Right
                drawLine(targetColor, Offset(right, top), Offset(right - cornerLength, top), cornerStroke)
                drawLine(targetColor, Offset(right, top), Offset(right, top + cornerLength), cornerStroke)

                // Bottom-Left
                drawLine(targetColor, Offset(left, bottom), Offset(left + cornerLength, bottom), cornerStroke)
                drawLine(targetColor, Offset(left, bottom), Offset(left, bottom - cornerLength), cornerStroke)

                // Bottom-Right
                drawLine(targetColor, Offset(right, bottom), Offset(right - cornerLength, bottom), cornerStroke)
                drawLine(targetColor, Offset(right, bottom), Offset(right, bottom - cornerLength), cornerStroke)

                // Subtle inner dotted frame
                drawRect(
                    color = targetColor.copy(alpha = 0.25f),
                    topLeft = Offset(left, top),
                    size = Size(boxWidth, boxHeight),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                )

                // Center crosshair
                val centerX = left + boxWidth / 2f
                val centerY = top + boxHeight / 2f
                val crosshairSize = 10.dp.toPx()

                drawLine(
                    targetColor,
                    Offset(centerX - crosshairSize, centerY),
                    Offset(centerX + crosshairSize, centerY),
                    1.5.dp.toPx()
                )
                drawLine(
                    targetColor,
                    Offset(centerX, centerY - crosshairSize),
                    Offset(centerX, centerY + crosshairSize),
                    1.5.dp.toPx()
                )
            }

            // 3. Electronic Horizon Leveler
            if (isLevelerEnabled) {
                val roll = orientation.rollDegrees
                val isLevel = abs(roll) <= 1.5f
                val levelColor = if (isLevel) Color(0xFF10B981) else Color(0xFFFFB74D)

                val centerY = canvasHeight / 2f
                val centerX = canvasWidth / 2f
                val levelBarLength = 90.dp.toPx()

                rotate(degrees = -roll, pivot = Offset(centerX, centerY)) {
                    // Left segment
                    drawLine(
                        levelColor,
                        Offset(centerX - levelBarLength, centerY),
                        Offset(centerX - 16.dp.toPx(), centerY),
                        2.5.dp.toPx()
                    )
                    // Right segment
                    drawLine(
                        levelColor,
                        Offset(centerX + 16.dp.toPx(), centerY),
                        Offset(centerX + levelBarLength, centerY),
                        2.5.dp.toPx()
                    )
                    // Center reference dot
                    drawCircle(
                        color = levelColor,
                        radius = 3.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                }
            }
        }

        // Bounding Box Label Badge
        targetBox?.let { box ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = ((box.ymin * 100).coerceAtLeast(10f)).dp * 4,
                        start = ((box.xmin * 100).coerceAtLeast(10f)).dp * 2.5f
                    )
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFF0F172A).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🎯 AI FRAME TARGET",
                        color = Color(0xFF10B981),
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Leveler Degree Badge
        if (isLevelerEnabled) {
            val roll = orientation.rollDegrees
            val isLevel = abs(roll) <= 1.5f
            val rollText = if (isLevel) "LEVEL 0.0°" else "${if (roll > 0) "+" else ""}${(roll * 10).roundToInt() / 10.0}°"

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (isLevel) Color(0xFF10B981).copy(alpha = 0.9f)
                            else Color(0xFF0F172A).copy(alpha = 0.75f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = rollText,
                        color = if (isLevel) Color.Black else Color(0xFFFFB74D),
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
