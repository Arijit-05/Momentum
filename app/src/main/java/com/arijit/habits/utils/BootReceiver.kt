package com.arijit.habits.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId)
                .collection("allHabits")
                .get()
                .addOnSuccessListener { snapshot ->
                    snapshot.documents.mapNotNull { doc ->
                        doc.toObject(com.arijit.habits.models.Habit::class.java)
                    }.forEach { habit ->
                        if (habit.hasReminder && habit.reminderTime.isNotEmpty()) {
                            ReminderUtils.scheduleHabitReminder(context, habit)
                        }
                    }
                }
        }
    }
} 