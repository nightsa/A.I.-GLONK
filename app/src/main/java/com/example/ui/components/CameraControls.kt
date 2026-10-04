package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraMode
import com.example.model.GridType

@Composable
fun CameraTopBar(
    gridType: GridType,
    isLevelerEnabled: Boolean,
    isTorchEnabled: Boolean,
    onToggleGrid: () -> Unit,
    onToggleLeveler: () -> Unit,
    onToggleTorch: () -> Unit,
    onOpenWebGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left controls: Torch & Grid
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleTorch,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.65f))
                    .testTag("torch_button")
            ) {
                Icon(
                    imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "เปิด/ปิดไฟแฟลช",
                    tint = if (isTorchEnabled) Color(0xFFFBBF24) else Color.White
                )
            }

            IconButton(
                onClick = onToggleGrid,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.65f))
                    .testTag("grid_button")
            ) {
                Icon(
                    imageVector = if (gridType != GridType.NONE) Icons.Default.Grid3x3 else Icons.Default.GridOff,
                    contentDescription = "สลับเส้นตาราง",
                    tint = if (gridType != GridType.NONE) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Center: App title pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.75f))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CamDirector AI",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Right controls: Leveler & Web Guide
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleLeveler,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.65f))
                    .testTag("leveler_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ScreenRotation,
                    contentDescription = "เปิด/ปิดเส้นระดับน้ำ",
                    tint = if (isLevelerEnabled) Color(0xFF10B981) else Color.White.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = onOpenWebGuide,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.65f))
                    .testTag("web_guide_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "คู่มือใช้งานผ่านเว็บ & ทุกอุปกรณ์",
                    tint = Color(0xFF60A5FA)
                )
            }
        }
    }
}

@Composable
fun ZoomControls(
    currentZoom: Float,
    recommendedZoom: Float,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val zoomLevels = listOf(0.6f, 1.0f, 2.0f, 3.0f, 5.0f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.75f))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        zoomLevels.forEach { zoom ->
            val isSelected = kotlin.math.abs(currentZoom - zoom) < 0.1f
            val isRecommended = kotlin.math.abs(recommendedZoom - zoom) < 0.2f

            val bgColor by animateColorAsState(
                targetValue = when {
                    isSelected -> Color(0xFF38BDF8)
                    isRecommended -> Color(0xFF1E293B)
                    else -> Color.Transparent
                },
                label = "zoomBg"
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(
                        width = if (isRecommended && !isSelected) 1.5.dp else 0.dp,
                        color = if (isRecommended && !isSelected) Color(0xFF10B981) else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onZoomSelected(zoom) }
                    .testTag("zoom_${zoom}x"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${if (zoom == 0.6f) ".6" else zoom.toInt()}x",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected || isRecommended) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else Color.White
                )
            }
        }
    }
}

@Composable
fun ModeSelectorBar(
    currentMode: CameraMode,
    onModeSelected: (CameraMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = CameraMode.values()
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        modes.forEach { mode ->
            val isSelected = mode == currentMode

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF0F172A).copy(alpha = 0.6f)
                    )
                    .clickable { onModeSelected(mode) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("mode_${mode.name.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mode.icon,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = mode.labelTh,
                        color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun CameraBottomActions(
    isAnalyzing: Boolean,
    onShutterClick: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenDirectorAdvice: () -> Unit,
    onPickImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Upload photo / Pick Image button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onPickImage() }
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.7f))
                    .border(1.dp, Color(0xFF475569), CircleShape)
                    .testTag("pick_photo_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "เลือกรูปภาพจากเครื่อง",
                    tint = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text("เลือกรูป", color = Color(0xFF94A3B8), fontSize = 10.sp)
        }

        // Center Pro Shutter Button with AI Glowing Ring
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF10B981), Color(0xFF3B82F6), Color(0xFF06B6D4))
                    )
                )
                .clickable(enabled = !isAnalyzing) { onShutterClick() }
                .padding(4.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F172A))
                .padding(4.dp)
                .clip(CircleShape)
                .background(if (isAnalyzing) Color(0xFF1E293B) else Color.White)
                .testTag("shutter_button"),
            contentAlignment = Alignment.Center
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color(0xFF10B981),
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "กดชัตเตอร์และวิเคราะห์ภาพด้วย AI",
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // AI Advice / Director sheet trigger
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { onOpenDirectorAdvice() }
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(1.5.dp, Color(0xFF10B981), CircleShape)
                    .testTag("ai_advice_sheet_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "ดูคำแนะนำการถ่ายภาพ",
                    tint = Color(0xFF10B981)
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text("คำแนะนำ", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
