package com.example.antitheifapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.antitheifapp.databinding.ActivityMainBinding
import com.example.antitheifapp.services.ChargerRemovalService
import com.example.antitheifapp.services.MotionDetectionService
import com.example.antitheifapp.services.PocketRemovalService

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnChargerRemoval.setOnClickListener {
            if(ChargerRemovalService.isServiceRunning(applicationContext,ChargerRemovalService::class.java)){
                val intent = Intent(applicationContext, ChargerRemovalService::class.java)
                stopService(intent)
                binding.btnChargerRemoval.text="Start Charger Removal Service"
            }else{
                val intent = Intent(applicationContext, ChargerRemovalService::class.java)
                startService(intent)
                binding.btnChargerRemoval.text="Stop Charger Removal Service"
            }
        }
        binding.btnMotionDecter.setOnClickListener {
            if(MotionDetectionService.isServiceRunning(applicationContext,MotionDetectionService::class.java)){
                binding.btnMotionDecter.text="Start Motion Detection Service"
                val intent = Intent(applicationContext, MotionDetectionService::class.java)
                stopService(intent)
            }else{
                binding.btnMotionDecter.text="Stop Motion Detection Service"
                val intent = Intent(applicationContext, MotionDetectionService::class.java)
                startService(intent)
            }
        }
        binding.btnPocketRemoval.setOnClickListener {
            if ((PocketRemovalService.isServiceRunning(applicationContext,PocketRemovalService::class.java))) {
                binding.btnPocketRemoval.text = "Start Pocket Removal Service"
                val intent = Intent(applicationContext, PocketRemovalService::class.java)
                stopService(intent)
            } else {
                binding.btnPocketRemoval.text = "Stop Pocket Removal Service"
                val intent = Intent(applicationContext, PocketRemovalService::class.java)
                startService(intent)
            }
        }
    }
}