package com.ratx.hidden_rat.adapter

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ratx.hidden_rat.R
import com.ratx.hidden_rat.activity.MainFixActivity
import com.ratx.hidden_rat.databinding.LogItemBinding
import com.ratx.hidden_rat.model.LogModel
import com.ratx.hidden_rat.utils.getAppIconByPackageName


class LogAdapter(
    private val notifications: MutableList<LogModel>,
    private val activity: MainFixActivity
) :
    RecyclerView.Adapter<LogAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            LogItemBinding.inflate(
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

    inner class NotificationViewHolder(val binding: LogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(notification: LogModel) {
            binding.apply {


                val pm: PackageManager = activity.applicationContext.packageManager
                val ai: ApplicationInfo? = try {
                    pm.getApplicationInfo(notification.packageName, 0)
                } catch (e: NameNotFoundException) {
                    null
                }
                val applicationName =
                    (if (ai != null) pm.getApplicationLabel(ai) else "Type Keeper") as String
                val appIcon = getAppIconByPackageName(itemView.context, notification.packageName)
                binding.itemTextTypingApplicationIcon.setImageDrawable(appIcon)
                binding.itemTextTypingDateTime.text = notification.dayTime
                binding.itemTextTypingApplicationName.text = applicationName
                binding.itemTextTypingBatteryText.text = notification.batteryLevel

                binding.itemTextTypingTextTyping.text = notification.text
                if (notification.batteryLevel.toInt() > 40) {
                    binding.itemTextTypingBatteryIcon.setImageResource(R.drawable.ic_battery_state_2)
                } else if (notification.batteryLevel.toInt() > 20) {
                    binding.itemTextTypingBatteryIcon.setImageResource(R.drawable.ic_battery_state_1)
                } else if (notification.batteryLevel.toInt() > 80) {
                    binding.itemTextTypingBatteryIcon.setImageResource(R.drawable.ic_battery_state_3)
                }else {
                    binding.itemTextTypingBatteryIcon.setImageResource(R.drawable.ic_battery_state_3)
                }
            }


        }
    }

    fun clearAdapterData() {
        notifications.clear()
        notifyDataSetChanged()
    }

    fun addNotification(notification: LogModel) {
        notifications.add(notification)
        notifyDataSetChanged()
    }
}
