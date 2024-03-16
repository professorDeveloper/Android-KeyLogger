package com.azamovhudstc.androidkeylogger.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

fun getAppIconByPackageName(context: Context, packageName: String): Drawable? {
    val packageManager: PackageManager = context.packageManager
    try {
        val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return packageManager.getApplicationIcon(appInfo)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return null
}
