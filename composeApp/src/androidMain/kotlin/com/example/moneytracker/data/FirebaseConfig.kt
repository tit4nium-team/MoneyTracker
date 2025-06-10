package com.example.moneytracker.data

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import android.content.Context
import com.example.moneytracker.BuildConfig

object FirebaseConfig {
    fun initialize(context: Context) {
        try {
            FirebaseApp.initializeApp(context)
        } catch (e: IllegalStateException) {
            // App already initialized
        }
    }
} 