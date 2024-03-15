package com.azamovhudstc.androidkeylogger

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class MyNotificationListenerService : NotificationListenerService() {
    private val notifications = mutableListOf<NotificationModel>()
    private lateinit var adapter: NotificationAdapter

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        // Handle notification posted
        // Handle notification posted
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        val packageName = sbn.packageName

        if (title != null && text != null) {
            Log.d("NotificationListener", "Package: $packageName, Title: $title, Text: $text")
            // Handle the received SMS or message here
                // MainActivity da updateRecyclerView ni chaqirish
                val intent = Intent("com.yourpackage.NOTIFICATION_LISTENER")
                intent.putExtra("packageName", packageName)
                intent.putExtra("title", title)
                intent.putExtra("text", text)
                sendBroadcast(intent)
//            saveNotificationToFile(NotificationModel(packageName, title, text))
        }
    }



    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

    }
}
