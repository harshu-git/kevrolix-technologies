package com.antiphonesnatcher.app.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * High-precision sensor monitor to detect phone snatching / theft attempts in real time.
 *
 * Designed with a warm-up grace period to prevent false alarms on app startup or sensor initialization,
 * and low-pass gravity filtering to prevent false triggers during ordinary movement, tilting, or walking.
 *
 * Snatch detection requires either:
 * 1. Sudden violent jerk / acceleration yank (> 21.0 m/s²)
 * 2. Or high linear acceleration (> 15.0 m/s²) combined with rapid rotational wrist twist (> 4.8 rad/s)
 */
class AntiSnatchSensorHelper(
    private val context: Context,
    private val onSnatchDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val linearAcceleration = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    private var isListening = false
    private var listenStartTime = 0L
    private var lastTriggerTime = 0L

    // Gyroscope tracking
    private var lastAngularSpeed = 0f

    // Gravity tracking for fallback accelerometer
    private val gravity = floatArrayOf(0f, 0f, SensorManager.GRAVITY_EARTH)
    private var hasGravityInit = false

    fun startListening() {
        if (isListening) return
        val sm = sensorManager ?: return

        listenStartTime = System.currentTimeMillis()
        hasGravityInit = false

        try {
            // SENSOR_DELAY_GAME provides ~20ms (50Hz) updates without triggering SecurityException
            linearAcceleration?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            } ?: run {
                accelerometer?.let {
                    sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
                }
            }
            gyroscope?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        isListening = true
    }

    fun stopListening() {
        if (!isListening) return
        val sm = sensorManager ?: return
        sm.unregisterListener(this)
        isListening = false
        hasGravityInit = false
    }

    fun resetWarmup() {
        listenStartTime = System.currentTimeMillis()
        hasGravityInit = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val now = System.currentTimeMillis()

        // 1. Warm-up grace period: Ignore initial sensor settling noise & startup transients
        if (now - listenStartTime < WARMUP_PERIOD_MS) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER && !hasGravityInit) {
                gravity[0] = event.values[0]
                gravity[1] = event.values[1]
                gravity[2] = event.values[2]
                hasGravityInit = true
            }
            return
        }

        // 2. Cooldown period: Prevent duplicate triggers within 2.5 seconds
        if (now - lastTriggerTime < COOLDOWN_MS) return

        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]
                val gy = event.values[1]
                val gz = event.values[2]
                lastAngularSpeed = sqrt(gx * gx + gy * gy + gz * gz)
            }

            Sensor.TYPE_LINEAR_ACCELERATION -> {
                val lx = event.values[0]
                val ly = event.values[1]
                val lz = event.values[2]
                val netLinearAccel = sqrt(lx * lx + ly * ly + lz * lz)

                // Snatch threshold: violent yank (>21.0 m/s²) or moderate jerk (>15.0 m/s²) + fast rotation (>4.8 rad/s)
                val isSnatch = netLinearAccel > HIGH_LINEAR_ACCEL_THRESHOLD ||
                    (netLinearAccel > MODERATE_LINEAR_ACCEL_THRESHOLD && lastAngularSpeed > GYRO_ROTATION_THRESHOLD)

                if (isSnatch) {
                    lastTriggerTime = now
                    onSnatchDetected()
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // Low-pass filter to isolate gravity and calculate pure dynamic acceleration
                val ax = event.values[0]
                val ay = event.values[1]
                val az = event.values[2]

                if (!hasGravityInit) {
                    gravity[0] = ax
                    gravity[1] = ay
                    gravity[2] = az
                    hasGravityInit = true
                    return
                }

                val alpha = 0.82f
                gravity[0] = alpha * gravity[0] + (1 - alpha) * ax
                gravity[1] = alpha * gravity[1] + (1 - alpha) * ay
                gravity[2] = alpha * gravity[2] + (1 - alpha) * az

                val dynX = ax - gravity[0]
                val dynY = ay - gravity[1]
                val dynZ = az - gravity[2]
                val dynamicAccel = sqrt(dynX * dynX + dynY * dynY + dynZ * dynZ)

                val isSnatch = dynamicAccel > HIGH_LINEAR_ACCEL_THRESHOLD ||
                    (dynamicAccel > MODERATE_LINEAR_ACCEL_THRESHOLD && lastAngularSpeed > GYRO_ROTATION_THRESHOLD)

                if (isSnatch) {
                    lastTriggerTime = now
                    onSnatchDetected()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    companion object {
        private const val WARMUP_PERIOD_MS = 400L // 400ms grace period on start/re-arm
        private const val COOLDOWN_MS = 2000L // Prevent double trigger
        private const val HIGH_LINEAR_ACCEL_THRESHOLD = 14.5f // m/s² snatch yank
        private const val MODERATE_LINEAR_ACCEL_THRESHOLD = 10.5f // m/s² yank with wrist rotation
        private const val GYRO_ROTATION_THRESHOLD = 2.6f // rad/s fast snatch twist
    }
}

