package com.example.model

import androidx.annotation.DrawableRes

enum class CameraMode(val labelTh: String, val labelEn: String, val icon: String) {
    AUTO("อัตโนมัติ", "Auto", "✨"),
    PORTRAIT("บุคคล", "Portrait", "👤"),
    FOOD("อาหาร", "Food", "🍰"),
    LANDSCAPE("วิวทิวทัศน์", "Landscape", "🌄"),
    PRODUCT("สินค้า", "Product", "📦"),
    NIGHT("กลางคืน", "Night", "🌙")
}

enum class GridType(val labelTh: String) {
    NONE("ปิดเส้น"),
    RULE_OF_THIRDS("กฎ 3 ส่วน"),
    GOLDEN_SPIRAL("ก้นหอยทองคำ")
}

data class NormalizedRect(
    val ymin: Float = 0.2f,
    val xmin: Float = 0.2f,
    val ymax: Float = 0.8f,
    val xmax: Float = 0.8f
)

data class CameraSettingsAdvice(
    val ev: String = "+0.0 EV",
    val iso: String = "ISO 100",
    val shutterSpeed: String = "1/250s",
    val aperture: String = "f/2.8",
    val flashMode: String = "ปิดแฟลช (ใช้แสงธรรมชาติ)",
    val whiteBalance: String = "Auto (5000K)"
)

data class ActionableStep(
    val id: Int,
    val title: String,
    val detail: String,
    var isCompleted: Boolean = false
)

data class AiDirectorAdvice(
    val sceneType: String,
    val compositionScore: Int,
    val focalPointSuggestion: String,
    val targetBoundingBox: NormalizedRect? = null,
    val recommendedZoom: String,
    val zoomFactor: Float = 1.0f,
    val distanceAdvice: String,
    val angleAdvice: String,
    val tiltLevelAdvice: String,
    val lightingAdvice: String,
    val cameraSettings: CameraSettingsAdvice = CameraSettingsAdvice(),
    val compositionRules: List<String> = emptyList(),
    val actionableSteps: List<ActionableStep> = emptyList(),
    val critiqueSummary: String
)

data class SampleSceneItem(
    val id: String,
    val titleTh: String,
    val category: CameraMode,
    @param:DrawableRes val drawableRes: Int,
    val description: String,
    val initialAdvice: AiDirectorAdvice
)
