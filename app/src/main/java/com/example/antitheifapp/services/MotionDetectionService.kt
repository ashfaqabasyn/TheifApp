package com.example.antitheifapp.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import kotlin.math.sqrt
class MotionDetectionService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var isRingtonePlaying = false // Flag to track whether the ringtone is currently playing

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val motionDetected = isMotionDetected(event.values[0], event.values[1], event.values[2])
            if (motionDetected && !isRingtonePlaying) {
                performMotionAction()
            }
        }
    }

    private fun isMotionDetected(x: Float, y: Float, z: Float): Boolean {
        val accelerationThreshold = 1.0f // Adjust as needed
        val totalAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        return totalAcceleration > accelerationThreshold
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performMotionAction() {
        try {
            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(applicationContext, defaultRingtoneUri)
            if (ringtone != null && !ringtone.isPlaying) {
                ringtone.play()
                isRingtonePlaying = true // Set the flag to indicate that the ringtone is playing
                Handler(Looper.getMainLooper()).postDelayed({
                    isRingtonePlaying = false
                }, 1000) // Adjust the delay as needed
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun isServiceRunning(context: Context, serviceClass: Class<*>) : Boolean {
            val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }
    }
}