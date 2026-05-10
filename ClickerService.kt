package com.cop.autoclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.*

class ClickerService : AccessibilityService() {

    companion object {
        var instance: ClickerService? = null
        var isRunning = false
    }

    var intervalMs = 100L
    private var clickJob: Job? = null
    var clickX = 540f
    var clickY = 1200f

    override fun onServiceConnected() {
        instance = this
    }

    fun startClicking() {
        isRunning = true
        clickJob = CoroutineScope(Dispatchers.Default).launch {
            while (isRunning) {
                performTap(clickX, clickY)
                delay(intervalMs)
            }
        }
    }

    fun stopClicking() {
        isRunning = false
        clickJob?.cancel()
    }

    private fun performTap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 1)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { stopClicking() }
    override fun onDestroy() {
        instance = null
        stopClicking()
        super.onDestroy()
    }
}
