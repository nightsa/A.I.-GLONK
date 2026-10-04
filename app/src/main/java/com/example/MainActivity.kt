package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CameraDirectorScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.MasterclassGuideScreen
import com.example.ui.screens.WebCompanionScreen
import com.example.ui.theme.CamDirectorTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.CameraDirectorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CamDirectorTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen(
    viewModel: CameraDirectorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedShots by viewModel.savedShots.collectAsStateWithLifecycle()

    // Handle back button when navigating secondary tabs
    BackHandler(enabled = uiState.activeTab != AppTab.CAMERA) {
        viewModel.setTab(AppTab.CAMERA)
    }

    // Android Zero-Permission Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.loadBitmapFromUri(uri)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A),
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.CAMERA,
                    onClick = { viewModel.setTab(AppTab.CAMERA) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "กล้อง AI Director"
                        )
                    },
                    label = { Text("กล้อง AI", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF10B981),
                        selectedTextColor = Color(0xFF10B981),
                        indicatorColor = Color(0xFF1E293B),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_camera")
                )

                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.GALLERY,
                    onClick = { viewModel.setTab(AppTab.GALLERY) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "แกลเลอรีและประวัติ"
                        )
                    },
                    label = { Text("ที่บันทึกไว้", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF60A5FA),
                        selectedTextColor = Color(0xFF60A5FA),
                        indicatorColor = Color(0xFF1E293B),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_gallery")
                )

                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.MASTERCLASS,
                    onClick = { viewModel.setTab(AppTab.MASTERCLASS) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "สูตรจัดองค์ประกอบภาพ"
                        )
                    },
                    label = { Text("สูตรถ่ายรูป", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFFBBF24),
                        selectedTextColor = Color(0xFFFBBF24),
                        indicatorColor = Color(0xFF1E293B),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_masterclass")
                )

                NavigationBarItem(
                    selected = uiState.activeTab == AppTab.WEB_GUIDE,
                    onClick = { viewModel.setTab(AppTab.WEB_GUIDE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "ใช้งานผ่านเว็บทุกอุปกรณ์"
                        )
                    },
                    label = { Text("ทุกอุปกรณ์", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        indicatorColor = Color(0xFF1E293B),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_web_guide")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0A0F1D))
        ) {
            when (uiState.activeTab) {
                AppTab.CAMERA -> {
                    CameraDirectorScreen(
                        uiState = uiState,
                        onToggleGrid = { viewModel.toggleGrid() },
                        onToggleLeveler = { viewModel.toggleLeveler() },
                        onToggleTorch = { viewModel.toggleTorch() },
                        onSetZoom = { zoom -> viewModel.setZoom(zoom) },
                        onSetMode = { mode -> viewModel.setCameraMode(mode) },
                        onSelectSampleScene = { scene -> viewModel.selectSampleScene(scene) },
                        onShutterClick = { viewModel.analyzeCurrentFrame() },
                        onOpenGallery = { viewModel.setTab(AppTab.GALLERY) },
                        onOpenWebGuide = { viewModel.setTab(AppTab.WEB_GUIDE) },
                        onToggleBottomSheet = { open -> viewModel.toggleBottomSheet(open) },
                        onSaveShot = { viewModel.saveCurrentShotAdvice() },
                        onToggleStep = { stepId -> viewModel.toggleActionableStep(stepId) },
                        onPickImage = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onLiveCameraFrame = { bitmap ->
                            viewModel.setLiveCameraBitmap(bitmap)
                            viewModel.analyzeBitmap(bitmap, uiState.activeMode)
                        },
                        onClearNotification = { viewModel.clearNotification() }
                    )
                }

                AppTab.GALLERY -> {
                    GalleryScreen(
                        shots = savedShots,
                        onDeleteShot = { id -> viewModel.deleteSavedShot(id) }
                    )
                }

                AppTab.MASTERCLASS -> {
                    MasterclassGuideScreen()
                }

                AppTab.WEB_GUIDE -> {
                    WebCompanionScreen(
                        onSelectSample = { scene ->
                            viewModel.selectSampleScene(scene)
                            viewModel.setTab(AppTab.CAMERA)
                        },
                        onSimulateTilt = { tilt ->
                            viewModel.setSimulatedTilt(tilt)
                        },
                        currentRoll = uiState.orientation.rollDegrees
                    )
                }
            }
        }
    }
}
