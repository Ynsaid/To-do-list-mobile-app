package com.example.todolist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskTitle = intent.getStringExtra("task_title") ?: "Upcoming Task"

        // Build the notification
        val builder = NotificationCompat.Builder(context, "task_notifications")
            .setSmallIcon(R.drawable.time) // Make sure you have an icon in your res/drawable folder
            .setContentTitle("Task Reminder")
            .setContentText("Your task '$taskTitle' is due in 10 minutes!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Show the notification
        with(NotificationManagerCompat.from(context)) {
            notify(1001, builder.build()) // Notification ID (should be unique per task)
        }
    }
}
