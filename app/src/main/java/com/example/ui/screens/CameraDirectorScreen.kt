package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.camera.CameraPreview
import com.example.model.CameraMode
import com.example.model.GridType
import com.example.model.SampleSceneItem
import com.example.ui.components.AiDirectorBottomSheet
import com.example.ui.components.CameraBottomActions
import com.example.ui.components.CameraOverlayHud
import com.example.ui.components.CameraTopBar
import com.example.ui.components.ModeSelectorBar
import com.example.ui.components.ZoomControls
import com.example.util.SampleScenesProvider
import com.example.viewmodel.CameraUiState

@Composable
fun CameraDirectorScreen(
    uiState: CameraUiState,
    onToggleGrid: () -> Unit,
    onToggleLeveler: () -> Unit,
    onToggleTorch: () -> Unit,
    onSetZoom: (Float) -> Unit,
    onSetMode: (CameraMode) -> Unit,
    onSelectSampleScene: (SampleSceneItem) -> Unit,
    onShutterClick: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenWebGuide: () -> Unit,
    onToggleBottomSheet: (Boolean?) -> Unit,
    onSaveShot: () -> Unit,
    onToggleStep: (Int) -> Unit,
    onPickImage: () -> Unit,
    onLiveCameraFrame: (Bitmap) -> Unit,
    onClearNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    var cameraCaptureTrigger by remember { mutableStateOf<(() -> Unit)?>(null) }
    var useLiveCameraFeed by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Viewfinder: Live Camera OR Selected Sample/Picked Photo
        Box(modifier = Modifier.fillMaxSize()) {
            if (useLiveCameraFeed && hasCameraPermission) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    zoomFactor = uiState.currentZoom,
                    isTorchEnabled = uiState.isTorchEnabled,
                    onFrameCaptured = { bitmap ->
                        onLiveCameraFrame(bitmap)
                    },
                    onCameraReady = { trigger ->
                        cameraCaptureTrigger = trigger
                    }
                )
            } else if (uiState.currentBitmap != null) {
                // Render sample or uploaded photo with zoom transform
                Image(
                    bitmap = uiState.currentBitmap.asImageBitmap(),
                    contentDescription = "ช่องมองภาพ",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = uiState.currentZoom.coerceIn(0.6f, 3.5f),
                            scaleY = uiState.currentZoom.coerceIn(0.6f, 3.5f)
                        )
                )
            } else {
                // Fallback placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("กำลังโหลดภาพ...", color = Color.White)
                }
            }

            // AR Overlay HUD (Rule of Thirds, Golden Spiral, AI Target Box, Leveler)
            CameraOverlayHud(
                modifier = Modifier.fillMaxSize(),
                gridType = uiState.gridType,
                isLevelerEnabled = uiState.isLevelerEnabled,
                orientation = uiState.orientation,
                targetBox = uiState.advice?.targetBoundingBox
            )
        }

        // 2. Top Controls Bar
        CameraTopBar(
            gridType = uiState.gridType,
            isLevelerEnabled = uiState.isLevelerEnabled,
            isTorchEnabled = uiState.isTorchEnabled,
            onToggleGrid = onToggleGrid,
            onToggleLeveler = onToggleLeveler,
            onToggleTorch = onToggleTorch,
            onOpenWebGuide = onOpenWebGuide,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3. Floating AI Director Quick Pill (Tap to expand full advice)
        uiState.advice?.let { advice ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 58.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.88f))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(20.dp))
                    .clickable { onToggleBottomSheet(true) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("floating_director_pill")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "คะแนน ${advice.compositionScore}/100 • แนะนำซูม ${advice.recommendedZoom}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ดูวิธีปรับ →",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Viewfinder Mode Switcher (Live Cam vs Sample Scenes)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 98.dp, start = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A).copy(alpha = 0.8f))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (useLiveCameraFeed) "กล้องสด (Live)" else "ฉากตัวอย่าง (Sample)",
                color = if (useLiveCameraFeed) Color(0xFF10B981) else Color(0xFF60A5FA),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E293B))
                    .clickable {
                        if (!useLiveCameraFeed && !hasCameraPermission) {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                        useLiveCameraFeed = !useLiveCameraFeed
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "สลับ",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }

        // 5. Bottom HUD Controls (Sample Carousel, Zoom, Mode, Shutter)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color(0xFF0A0F1D).copy(alpha = 0.85f))
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick Sample Scenes Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ฉากทดสอบ:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                SampleScenesProvider.sampleScenes.forEach { scene ->
                    val isSelected = uiState.selectedSample?.id == scene.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) Color(0xFF1E3A8A) else Color(0xFF1E293B)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF60A5FA) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                useLiveCameraFeed = false
                                onSelectSampleScene(scene)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("scene_chip_${scene.id}")
                    ) {
                        Text(
                            text = "${scene.category.icon} ${scene.titleTh.take(15)}...",
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Zoom Controller Pill
            ZoomControls(
                currentZoom = uiState.currentZoom,
                recommendedZoom = uiState.advice?.zoomFactor ?: 1.0f,
                onZoomSelected = onSetZoom
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Mode Selector Bar (AUTO, PORTRAIT, FOOD, LANDSCAPE, PRODUCT, NIGHT)
            ModeSelectorBar(
                currentMode = uiState.activeMode,
                onModeSelected = onSetMode
            )

            // Shutter Button & Bottom Actions
            CameraBottomActions(
                isAnalyzing = uiState.isAnalyzing,
                onShutterClick = {
                    if (useLiveCameraFeed && cameraCaptureTrigger != null) {
                        cameraCaptureTrigger?.invoke()
                    } else {
                        onShutterClick()
                    }
                },
                onOpenGallery = onOpenGallery,
                onOpenDirectorAdvice = { onToggleBottomSheet(true) },
                onPickImage = onPickImage
            )
        }

        // Notification Snackbar
        uiState.userNotification?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                onClearNotification()
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = msg,
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Expandable AI Director Bottom Sheet
        AnimatedVisibility(
            visible = uiState.isBottomSheetOpen && uiState.advice != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            uiState.advice?.let { advice ->
                AiDirectorBottomSheet(
                    advice = advice,
                    onClose = { onToggleBottomSheet(false) },
                    onApplyZoom = { zoom ->
                        onSetZoom(zoom)
                        onToggleBottomSheet(false)
                    },
                    onSaveShot = onSaveShot,
                    onToggleStep = onToggleStep
                )
            }
        }
    }
}
