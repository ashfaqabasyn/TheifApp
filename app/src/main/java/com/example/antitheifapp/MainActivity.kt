package com.example.antitheifapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.antitheifapp.databinding.ActivityMainBinding
import com.example.antitheifapp.services.ThiefService
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var isPocketEnabled = false
    private var isMotionEnabled = false
    private var isChargerEnabled = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),0)
        }

        sharedPreferences = getSharedPreferences("DetectionPrefs", Context.MODE_PRIVATE)
        // Retrieve saved values from SharedPreferences
        isPocketEnabled = sharedPreferences.getBoolean("isPocketEnabled", false)
        isMotionEnabled = sharedPreferences.getBoolean("isMotionEnabled", false)
        isChargerEnabled = sharedPreferences.getBoolean("isChargerEnabled", false)

        // Update UI based on saved values
        updateUI()
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            binding.btnChargerRemoval.setOnClickListener {
                if (isChargerEnabled) {
                    disableChargerRemoval()
                } else {
                    enableChargerRemoval()
                }
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            binding.btnMotionDecter.setOnClickListener {
                if (isMotionEnabled) {
                    disableMotionDetection()
                } else {
                    enableMotionDetection()
                }
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            binding.btnPocketRemoval.setOnClickListener {
                if (isPocketEnabled) {
                    disablePocketDetection()
                } else {
                    enablePocketDetection()
                }
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this@MainActivity, "Granted", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@MainActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
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
