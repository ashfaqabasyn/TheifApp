package com.example.antitheifapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.antitheifapp.databinding.ActivityMainBinding
import com.example.antitheifapp.services.ThiefService
import android.content.Context
import android.content.SharedPreferences

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isPocketEnabled = false
    private var isMotionEnabled = false
    private var isChargerEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        sharedPreferences = getSharedPreferences("DetectionPrefs", Context.MODE_PRIVATE)
        // Retrieve saved values from SharedPreferences
        isPocketEnabled = sharedPreferences.getBoolean("isPocketEnabled", false)
        isMotionEnabled = sharedPreferences.getBoolean("isMotionEnabled", false)
        isChargerEnabled = sharedPreferences.getBoolean("isChargerEnabled", false)

        // Update UI based on saved values
        updateUI()

        binding.btnChargerRemoval.setOnClickListener {
            if (isChargerEnabled) {
                disableChargerRemoval()
            } else {
                enableChargerRemoval()
            }
        }

        binding.btnMotionDecter.setOnClickListener {
            if (isMotionEnabled) {
                disableMotionDetection()
            } else {
                enableMotionDetection()
            }
        }

        binding.btnPocketRemoval.setOnClickListener {
            if (isPocketEnabled) {
                disablePocketDetection()
            } else {
                enablePocketDetection()
            }
        }
    }

    private fun enablePocketDetection() {
        val intent = Intent(this, ThiefService::class.java)
        intent.action = ThiefService.ACTION_POCKET_REMOVED
        startService(intent)
        isPocketEnabled = true
        updateUI()
        savePreferences()
    }

    private fun disablePocketDetection() {
        val intent = Intent(this, ThiefService::class.java)
        intent.action = ThiefService.ACTION_POCKET_REMOVED
        stopService(intent)
        isPocketEnabled = false
        updateUI()
        savePreferences()
        stopServiceIfAllDisabled()
    }

    private fun enableMotionDetection() {
        val intent = Intent(this, ThiefService::class.java)
        intent.action = ThiefService.ACTION_MOTION_DETECTED
        startService(intent)
        isMotionEnabled = true
        updateUI()
        savePreferences()
    }

    private fun disableMotionDetection() {
        val intent = Intent(this, ThiefService::class.java)
        intent.action = ThiefService.ACTION_MOTION_DETECTED
        stopService(intent)
        isMotionEnabled = false
        updateUI()
        savePreferences()
        stopServiceIfAllDisabled()

    }

    private fun enableChargerRemoval() {
        val intent = Intent(this, ThiefService::class.java)
        intent.action = ThiefService.ACTION_CHARGER_REMOVED
        startService(intent)
        isChargerEnabled = true
        updateUI()
        savePreferences()
    }

    private fun disableChargerRemoval() {
        val intent = Intent(this, ThiefService::class.java)
        intent.action = ThiefService.ACTION_CHARGER_REMOVED
        stopService(intent)
        isChargerEnabled = false
        updateUI()
        savePreferences()
        stopServiceIfAllDisabled()

    }

    private fun updateUI() {
        binding.btnPocketRemoval.text =
            if (isPocketEnabled) "Disable Pocket Detection" else "Enable Pocket Detection"
        binding.btnMotionDecter.text =
            if (isMotionEnabled) "Disable Motion Detection" else "Enable Motion Detection"
        binding.btnChargerRemoval.text =
            if (isChargerEnabled) "Disable Charger Removal" else "Enable Charger Removal"
    }

    private fun savePreferences() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isPocketEnabled", isPocketEnabled)
        editor.putBoolean("isMotionEnabled", isMotionEnabled)
        editor.putBoolean("isChargerEnabled", isChargerEnabled)
        editor.apply()
    }

    private fun stopServiceIfAllDisabled() {
        if (!isPocketEnabled && !isMotionEnabled && !isChargerEnabled) {
            val serviceIntent = Intent(this, ThiefService::class.java)
            stopService(serviceIntent)
        }
    }
}
