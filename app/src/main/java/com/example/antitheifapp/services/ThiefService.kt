package com.example.antitheifapp.services

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.core.app.NotificationCompat
import com.example.antitheifapp.R

class ThiefService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var proximitySensor: Sensor
    private lateinit var chargerReceiver: ChargerReceiver
    private var isPocketRemoved = false
    private var lastZAcceleration = 0f
    private var isRingtonePlaying = false
    private val notificationId = 1
    private val notificationChannelId = "Service_channel"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)!!
        chargerReceiver = ChargerReceiver()

        // Create notification channel
        createNotificationChannel()
        // Start service as foreground service
        startForegroundService()

        // Register sensor listeners
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)

        // Register the BroadcastReceiver for power disconnect and screen off events
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(chargerReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister sensor listeners
        sensorManager.unregisterListener(this)
        // Unregister charger receiver
        unregisterReceiver(chargerReceiver)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val zAcceleration = event.values[2]
                if (!isPocketRemoved && isRemovedFromPocket(zAcceleration)) {
                    // Pocket removal detected, perform action
                    performPocketRemovalAction()
                    sendPocketRemovedBroadcast()
                    isPocketRemoved = true
                } else if (isPocketRemoved && isReturnedToPocket(zAcceleration)) {
                    isPocketRemoved = false
                }
                lastZAcceleration = zAcceleration
            }
            Sensor.TYPE_PROXIMITY -> {
                if (event.values[0] == 0f && !isRingtonePlaying) {
                    // Motion detection detected, perform action
                    performMotionAction()
                    sendMotionDetectedBroadcast()
                }
            }
        }
    }

    private fun isRemovedFromPocket(zAcceleration: Float): Boolean {
        // Check if device has been removed from pocket based on acceleration change
        val accelerationThreshold = 5f // Adjust as needed
        return zAcceleration < lastZAcceleration - accelerationThreshold
    }

    private fun isReturnedToPocket(zAcceleration: Float): Boolean {
        // Check if device has returned to pocket based on acceleration change
        val accelerationThreshold = 5f // Adjust as needed
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun performMotionAction() {
        try {
            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(applicationContext, defaultRingtoneUri)
            if (ringtone != null && !ringtone.isPlaying) {
                ringtone.play()
                isRingtonePlaying = true
                Handler(Looper.getMainLooper()).postDelayed({
                    isRingtonePlaying = false
                }, 1000) // Adjust the delay as needed
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            notificationChannelId,
            "Combined Detection Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun startForegroundService() {
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Thief Service")
            .setContentText("Thief service is running.")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setAutoCancel(true)

        val notification = notificationBuilder.build()
        startForeground(notificationId, notification)
    }

    private fun sendPocketRemovedBroadcast() {
        val intent = Intent(ACTION_POCKET_REMOVED)
        sendBroadcast(intent)
    }

    private fun sendMotionDetectedBroadcast() {
        val intent = Intent(ACTION_MOTION_DETECTED)
        sendBroadcast(intent)
    }

    inner class ChargerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_DISCONNECTED -> {
                    try {
                        if (!isRingtonePlaying) {
                            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                            val ringtone = RingtoneManager.getRingtone(applicationContext, defaultRingtoneUri)
                            if (ringtone != null && !ringtone.isPlaying) {
                                ringtone.play()
                                isRingtonePlaying = true
                            }
                            displayNotification()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    stopRingtone()
                }
            }
        }
    }

    private fun displayNotification() {
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Charger Disconnected")
            .setContentText("Charger has been disconnected.")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()

        startForeground(notificationId, notification)
    }

    private fun stopRingtone() {
        try {
            val defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(applicationContext, defaultRingtoneUri)
            if (ringtone != null && ringtone.isPlaying) {
                ringtone.stop()
                isRingtonePlaying = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val ACTION_POCKET_REMOVED = "com.example.app.ACTION_POCKET_REMOVED"
        const val ACTION_MOTION_DETECTED = "com.example.app.ACTION_MOTION_DETECTED"
        const val ACTION_CHARGER_REMOVED = "com.example.app.ACTION_CHARGER_REMOVED"
    }
}