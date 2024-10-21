package com.ratx.hidden_rat.adapter

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ratx.hidden_rat.databinding.NotificationItemBinding
import com.ratx.hidden_rat.model.NotificationModel
import com.ratx.hidden_rat.utils.getAppIconByPackageName

class NotificationAdapter(private val notifications: MutableList<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            NotificationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    inner class NotificationViewHolder(val binding: NotificationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationModel) {
            binding.apply {

                val pm: PackageManager = itemView.context.applicationContext.packageManager
                val ai: ApplicationInfo? = try {
                    pm.getApplicationInfo(notification.packageName, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    null
                }
                val applicationName = (if (ai != null) pm.getApplicationLabel(ai) else "Type Keeper") as String
                val appIcon = getAppIconByPackageName(itemView.context, notification.packageName)
                binding.appIcon.setImageDrawable(appIcon)
                binding.appDescription.text = notification.text
                binding.appTime.text = notification.timeStamp
                binding.appName.text = notification.title
            }


        }
    }

    fun clearAdapterData() {
        notifications.clear()
        notifyDataSetChanged()
    }

    fun addNotification(notification: NotificationModel) {
        notifications.add(notification)
        notifyDataSetChanged()
    }
}
