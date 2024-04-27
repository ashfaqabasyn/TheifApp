package com.example.antitheifapp.services

import android.app.Service
import android.os.*
import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi


class PocketRemovalService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var isPocketRemoved = false
    private var lastZAcceleration = 0f
    private val accelerationThreshold = 5f // Adjust as needed

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
            val zAcceleration = event.values[2]
            if (!isPocketRemoved) {
                if (isRemovedFromPocket(zAcceleration)) {
                    // Pocket removal detected, perform action
                    performPocketRemovalAction()
                    isPocketRemoved = true
                }
            } else {
                // Check if returned to pocket
                if (isReturnedToPocket(zAcceleration)) {
                    isPocketRemoved = false
                }
            }
            lastZAcceleration = zAcceleration
        }
    }

    private fun isRemovedFromPocket(zAcceleration: Float): Boolean {
        // Check if device has been removed from pocket based on acceleration change
        return zAcceleration < lastZAcceleration - accelerationThreshold
    }

    private fun isReturnedToPocket(zAcceleration: Float): Boolean {
        // Check if device has returned to pocket based on acceleration change
        return zAcceleration > lastZAcceleration + accelerationThreshold
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performPocketRemovalAction() {
        try {
            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(applicationContext, defaultRingtoneUri)
            if (ringtone != null && !ringtone.isPlaying) {
                ringtone.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun isServiceRunning(context: Context, serviceClass: Class<*>) : Boolean {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }
    }
}
