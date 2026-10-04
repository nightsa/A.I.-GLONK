package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_shots")
data class ShotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String,
    val score: Int,
    val focalPoint: String,
    val recommendedZoom: String,
    val distanceAdvice: String,
    val angleAdvice: String,
    val lightingAdvice: String,
    val evSetting: String,
    val critiqueSummary: String,
    val imageUri: String? = null
)
