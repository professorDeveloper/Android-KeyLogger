package com.azamovhudstc.androidkeylogger

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azamovhudstc.androidkeylogger.databinding.NotificationItemBinding
import com.azamovhudstc.androidkeylogger.utils.getAppIconByPackageName

class NotificationAdapter(private val notifications: MutableList<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(NotificationItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    inner class NotificationViewHolder(val binding: NotificationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationModel){
            binding.apply {
                val appIcon = getAppIconByPackageName(itemView.context, notification.packageName)
                binding.appIcon.setImageDrawable(appIcon)
                binding.appDescription.text = notification.text
                binding.appTime.text = notification.timeStamp
                binding.appName.text = notification.title
            }


        }
    }

    fun addNotification(notification: NotificationModel) {
        notifications.add(notification)
        notifyDataSetChanged()
    }
}
