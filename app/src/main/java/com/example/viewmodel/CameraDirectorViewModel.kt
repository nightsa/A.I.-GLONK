package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ShotEntity
import com.example.model.AiDirectorAdvice
import com.example.model.CameraMode
import com.example.model.GridType
import com.example.model.SampleSceneItem
import com.example.network.GeminiCameraService
import com.example.util.DeviceOrientationSensor
import com.example.util.OrientationData
import com.example.util.SampleScenesProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    CAMERA,
    GALLERY,
    MASTERCLASS,
    WEB_GUIDE
}

data class CameraUiState(
    val activeMode: CameraMode = CameraMode.AUTO,
    val gridType: GridType = GridType.RULE_OF_THIRDS,
    val isLevelerEnabled: Boolean = true,
    val isTorchEnabled: Boolean = false,
    val currentZoom: Float = 1.0f,
    val orientation: OrientationData = OrientationData(),
    val isAnalyzing: Boolean = false,
    val advice: AiDirectorAdvice? = null,
    val currentBitmap: Bitmap? = null,
    val selectedSample: SampleSceneItem? = null,
    val isCameraHardwareAvailable: Boolean = true,
    val activeTab: AppTab = AppTab.CAMERA,
    val isBottomSheetOpen: Boolean = false,
    val userNotification: String? = null
)

class CameraDirectorViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val shotDao = db.shotDao()
    private val orientationSensor = DeviceOrientationSensor(application)

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    val savedShots: StateFlow<List<ShotEntity>> = shotDao.getAllShots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Collect sensor updates
        viewModelScope.launch {
            orientationSensor.orientationData.collect { data ->
                _uiState.value = _uiState.value.copy(orientation = data)
            }
        }
        orientationSensor.startListening()

        // Set initial sample scene for instant rich preview
        selectSampleScene(SampleScenesProvider.sampleScenes.first())
    }

    override fun onCleared() {
        super.onCleared()
        orientationSensor.stopListening()
    }

    fun setSimulatedTilt(degrees: Float) {
        orientationSensor.setSimulatedRoll(degrees)
    }

    fun setTab(tab: AppTab) {
        _uiState.value = _uiState.value.copy(activeTab = tab)
    }

    fun setCameraMode(mode: CameraMode) {
        _uiState.value = _uiState.value.copy(activeMode = mode)
        // Auto update heuristic advice for active mode if current advice is generic
        val currentBmp = _uiState.value.currentBitmap
        if (currentBmp != null && !_uiState.value.isAnalyzing) {
            analyzeBitmap(currentBmp, mode)
        }
    }

    fun setZoom(factor: Float) {
        _uiState.value = _uiState.value.copy(currentZoom = factor)
    }

    fun toggleGrid() {
        val nextGrid = when (_uiState.value.gridType) {
            GridType.RULE_OF_THIRDS -> GridType.GOLDEN_SPIRAL
            GridType.GOLDEN_SPIRAL -> GridType.NONE
            GridType.NONE -> GridType.RULE_OF_THIRDS
        }
        _uiState.value = _uiState.value.copy(gridType = nextGrid)
    }

    fun toggleLeveler() {
        _uiState.value = _uiState.value.copy(isLevelerEnabled = !_uiState.value.isLevelerEnabled)
    }

    fun toggleTorch() {
        _uiState.value = _uiState.value.copy(isTorchEnabled = !_uiState.value.isTorchEnabled)
    }

    fun toggleBottomSheet(open: Boolean? = null) {
        val newState = open ?: !_uiState.value.isBottomSheetOpen
        _uiState.value = _uiState.value.copy(isBottomSheetOpen = newState)
    }

    fun selectSampleScene(scene: SampleSceneItem) {
        val context = getApplication<Application>()
        try {
            val bmp = BitmapFactory.decodeResource(context.resources, scene.drawableRes)
            _uiState.value = _uiState.value.copy(
                selectedSample = scene,
                currentBitmap = bmp,
                activeMode = scene.category,
                advice = scene.initialAdvice,
                currentZoom = scene.initialAdvice.zoomFactor,
                isBottomSheetOpen = true,
                userNotification = "เลือกตัวอย่าง: ${scene.titleTh}"
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                selectedSample = scene,
                activeMode = scene.category,
                advice = scene.initialAdvice,
                isBottomSheetOpen = true
            )
        }
    }

    fun loadBitmapFromUri(uri: Uri) {
        val context = getApplication<Application>()
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        _uiState.value = _uiState.value.copy(
                            currentBitmap = bitmap,
                            selectedSample = null,
                            activeTab = AppTab.CAMERA,
                            userNotification = "โหลดรูปภาพสำเร็จ กำลังวิเคราะห์..."
                        )
                        analyzeBitmap(bitmap, _uiState.value.activeMode)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    userNotification = "ไม่สามารถโหลดรูปภาพได้: ${e.localizedMessage}"
                )
            }
        }
    }

    fun setLiveCameraBitmap(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            currentBitmap = bitmap,
            selectedSample = null
        )
    }

    fun analyzeCurrentFrame() {
        val bmp = _uiState.value.currentBitmap
        if (bmp != null) {
            analyzeBitmap(bmp, _uiState.value.activeMode)
        } else {
            // If no frame is captured, use a sample scene
            selectSampleScene(SampleScenesProvider.sampleScenes.first())
        }
    }

    fun analyzeBitmap(bitmap: Bitmap, mode: CameraMode) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true)
            val result = GeminiCameraService.analyzeShot(bitmap, mode)
            _uiState.value = _uiState.value.copy(
                isAnalyzing = false,
                advice = result,
                isBottomSheetOpen = true,
                userNotification = "วิเคราะห์เสร็จสิ้น: คะแนน ${result.compositionScore}/100"
            )
        }
    }

    fun toggleActionableStep(stepId: Int) {
        val currentAdvice = _uiState.value.advice ?: return
        val updatedSteps = currentAdvice.actionableSteps.map { step ->
            if (step.id == stepId) step.copy(isCompleted = !step.isCompleted) else step
        }
        _uiState.value = _uiState.value.copy(
            advice = currentAdvice.copy(actionableSteps = updatedSteps)
        )
    }

    fun saveCurrentShotAdvice() {
        val advice = _uiState.value.advice ?: return
        viewModelScope.launch {
            val entity = ShotEntity(
                title = "${advice.sceneType} - ${advice.compositionScore} คะแนน",
                mode = _uiState.value.activeMode.labelTh,
                score = advice.compositionScore,
                focalPoint = advice.focalPointSuggestion,
                recommendedZoom = advice.recommendedZoom,
                distanceAdvice = advice.distanceAdvice,
                angleAdvice = advice.angleAdvice,
                lightingAdvice = advice.lightingAdvice,
                evSetting = advice.cameraSettings.ev,
                critiqueSummary = advice.critiqueSummary
            )
            shotDao.insertShot(entity)
            _uiState.value = _uiState.value.copy(userNotification = "บันทึกคำแนะนำเข้าแกลเลอรีเรียบร้อย")
        }
    }

    fun deleteSavedShot(id: Long) {
        viewModelScope.launch {
            shotDao.deleteShotById(id)
            _uiState.value = _uiState.value.copy(userNotification = "ลบรายการเรียบร้อย")
        }
    }

    fun clearNotification() {
        _uiState.value = _uiState.value.copy(userNotification = null)
    }
}
