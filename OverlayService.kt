package com.cop.autoclicker

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.TextView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var targetView: View? = null
    private var isPickingTarget = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showControlPanel()
        return START_STICKY
    }

    private fun showControlPanel() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#CC1a1a2e"))
            setPadding(20, 20, 20, 20)
        }

        val tvTitle = TextView(this).apply {
            text = "🎯 Cop AutoClicker"
            setTextColor(Color.WHITE)
            textSize = 14f
        }

        val tvTarget = TextView(this).apply {
            text = "Hedef: Seçilmedi"
            setTextColor(Color.YELLOW)
            textSize = 11f
        }

        val btnPick = Button(this).apply {
            text = "📍 Hedef Seç"
            setBackgroundColor(Color.parseColor("#e94560"))
            setTextColor(Color.WHITE)
        }

        val btnToggle = Button(this).apply {
            text = "▶ Başlat"
            setBackgroundColor(Color.parseColor("#0f3460"))
            setTextColor(Color.WHITE)
        }

        val btnClose = Button(this).apply {
            text = "✕ Kapat"
            setBackgroundColor(Color.DKGRAY)
            setTextColor(Color.WHITE)
        }

        // Hedef seçme modu
        btnPick.setOnClickListener {
            showTargetPicker(tvTarget)
        }

        // Başlat / Durdur
        btnToggle.setOnClickListener {
            val service = ClickerService.instance ?: return@setOnClickListener
            if (ClickerService.isRunning) {
                service.stopClicking()
                btnToggle.text = "▶ Başlat"
                btnToggle.setBackgroundColor(Color.parseColor("#0f3460"))
            } else {
                service.startClicking()
                btnToggle.text = "⏹ Durdur"
                btnToggle.setBackgroundColor(Color.RED)
            }
        }

        btnClose.setOnClickListener {
            ClickerService.instance?.stopClicking()
            stopSelf()
        }

        layout.addView(tvTitle)
        layout.addView(tvTarget)
        layout.addView(btnPick)
        layout.addView(btnToggle)
        layout.addView(btnClose)

        val params = WindowManager.LayoutParams(
            350, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20; y = 100
        }

        // Sürüklenebilir yap
        layout.setOnTouchListener(object : View.OnTouchListener {
            var ix = 0f; var iy = 0f
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { ix = e.rawX - params.x; iy = e.rawY - params.y }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = (e.rawX - ix).toInt()
                        params.y = (e.rawY - iy).toInt()
                        windowManager.updateViewLayout(layout, params)
                    }
                }
                return false
            }
        })

        overlayView = layout
        windowManager.addView(layout, params)
    }

    private fun showTargetPicker(tvTarget: TextView) {
        // Yarı saydam crosshair overlay göster
        val crosshair = View(this).apply {
            setBackgroundColor(Color.parseColor("#880000FF"))
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        crosshair.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val x = event.rawX
                val y = event.rawY
                ClickerService.instance?.clickX = x
                ClickerService.instance?.clickY = y
                tvTarget.text = "Hedef: (${x.toInt()}, ${y.toInt()})"
                windowManager.removeView(crosshair)
            }
            true
        }

        targetView = crosshair
        windowManager.addView(crosshair, params)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        overlayView?.let { windowManager.removeView(it) }
        targetView?.let { windowManager.removeView(it) }
        super.onDestroy()
    }
}
