package com.arijit.habits.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.arijit.habits.MainActivity
import com.arijit.habits.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitName = intent.getStringExtra("habitName") ?: "Habit Reminder"
        val habitEmoji = intent.getStringExtra("habitEmoji") ?: "✒️"

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

        // Create an intent to launch the app's MainActivity
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d("ReminderReceiver", "Alarm triggered for: $habitName")

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Reminder")
            .setContentText("Time to complete: $habitEmoji $habitName")
            .setSmallIcon(R.drawable.fire)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Launch app on tap
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
