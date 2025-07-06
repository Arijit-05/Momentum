package com.arijit.habits.utils

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class Momentum: Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Log.d("FirebaseInit", "FirebaseApp INITIALIZED IN Momentum")
    }
}