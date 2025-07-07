package com.arijit.habits.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.arijit.habits.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitName = intent.getStringExtra("habitName") ?: "Habit Reminder"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "habit_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        Log.d("ReminderReceiver", "Alarm triggered for: $habitName")

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Reminder")
            .setContentText("Time to complete: $habitName")
            .setSmallIcon(R.drawable.add) // Replace with your own icon
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
