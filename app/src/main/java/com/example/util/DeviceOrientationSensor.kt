package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.sqrt

data class OrientationData(
    val rollDegrees: Float = 0f,
    val pitchDegrees: Float = 0f,
    val isLevel: Boolean = true,
    val hasHardwareSensor: Boolean = false
)

class DeviceOrientationSensor(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _orientationData = MutableStateFlow(
        OrientationData(
            rollDegrees = 0f,
            pitchDegrees = 0f,
            isLevel = true,
            hasHardwareSensor = rotationSensor != null
        )
    )
    val orientationData: StateFlow<OrientationData> = _orientationData.asStateFlow()

    private var simulatedRoll: Float = 0f

    fun startListening() {
        rotationSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun setSimulatedRoll(roll: Float) {
        simulatedRoll = roll
        val isLevel = kotlin.math.abs(roll) <= 1.5f
        _orientationData.value = _orientationData.value.copy(
            rollDegrees = roll,
            isLevel = isLevel
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
            val isLevel = kotlin.math.abs(roll) <= 1.8f

            _orientationData.value = OrientationData(
                rollDegrees = roll,
                pitchDegrees = pitch,
                isLevel = isLevel,
                hasHardwareSensor = true
            )
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]

            val roll = Math.toDegrees(atan2(ax.toDouble(), ay.toDouble())).toFloat()
            val pitch = Math.toDegrees(atan2(-az.toDouble(), sqrt((ax * ax + ay * ay).toDouble()))).toFloat()
            val isLevel = kotlin.math.abs(roll) <= 2.0f

            _orientationData.value = OrientationData(
                rollDegrees = roll,
                pitchDegrees = pitch,
                isLevel = isLevel,
                hasHardwareSensor = true
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
