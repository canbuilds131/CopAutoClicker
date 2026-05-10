package com.cop.autoclicker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val etInterval = findViewById<EditText>(R.id.etInterval)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnAccessibility = findViewById<Button>(R.id.btnAccessibility)
        val btnLaunch = findViewById<Button>(R.id.btnLaunch)

        // Overlay izni
        btnOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")))
            } else {
                Toast.makeText(this, "İzin zaten verilmiş ✓", Toast.LENGTH_SHORT).show()
            }
        }

        // Accessibility izni
        btnAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        // Overlay servisi başlat
        btnLaunch.setOnClickListener {
            val interval = etInterval.text.toString().toLongOrNull() ?: 100L
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Önce overlay iznini ver!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ClickerService.instance == null) {
                Toast.makeText(this, "Önce Erişilebilirlik servisini aç!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            ClickerService.instance?.intervalMs = interval
            val intent = Intent(this, OverlayService::class.java)
            intent.putExtra("interval", interval)
            startService(intent)
            tvStatus.text = "✅ Overlay açıldı! Yeşil butona bas."
        }
    }
}
