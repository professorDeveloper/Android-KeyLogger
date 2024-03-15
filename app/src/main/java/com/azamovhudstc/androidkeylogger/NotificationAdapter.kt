package com.azamovhudstc.androidkeylogger

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificationAdapter(private val notifications: MutableList<NotificationModel>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val packageNameTextView: TextView = itemView.findViewById(R.id.packageNameTextView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val textTextView: TextView = itemView.findViewById(R.id.textTextView)

        fun bind(notification: NotificationModel) {
            packageNameTextView.text = notification.packageName
            titleTextView.text = notification.title
            textTextView.text = notification.text
        }
    }

    fun addNotification(notification: NotificationModel) {
        notifications.add(notification)
        notifyDataSetChanged()
    }
}
