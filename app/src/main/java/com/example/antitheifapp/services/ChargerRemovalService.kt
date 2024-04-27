package com.example.antitheifapp.services

import android.app.ActivityManager

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.RingtoneManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast

class ChargerRemovalService : Service() {
    private val chargerReceiver = ChargerReceiver()
    private var isRingtonePlaying = false // Flag to track whether the ringtone is currently playing
    override fun onCreate() {
        super.onCreate()
        // Register the BroadcastReceiver for power disconnect and screen off events
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(chargerReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(chargerReceiver)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
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
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                Intent.ACTION_SCREEN_OFF -> {
                    // Stop the ringtone when the screen is turned off
                    stopRingtone()
                    Log.d("TAG33333345", "onReceive: ")

                }
            }
        }
    }

    private fun stopRingtone() {
        Toast.makeText(this@ChargerRemovalService, "hello", Toast.LENGTH_SHORT).show()
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

