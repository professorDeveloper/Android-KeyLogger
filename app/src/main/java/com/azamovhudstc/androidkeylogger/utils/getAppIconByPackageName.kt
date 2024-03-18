package com.azamovhudstc.androidkeylogger.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.azamovhudstc.androidkeylogger.model.NotificationModel

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

fun getPackageNamesFromNotifications(
    context: Context,
    notifications: ArrayList<NotificationModel>
): ArrayList<Pair<String, String>> {
    val packageNames = arrayListOf<Pair<String, String>>()

    for (notification in notifications) {
        val packageName = notification.packageName
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val label = packageManager.getApplicationLabel(applicationInfo).toString()
        if (!packageNames.any { it.first == packageName }) {
            packageNames.add(Pair(packageName, label))
        }
    }

    return packageNames
}
