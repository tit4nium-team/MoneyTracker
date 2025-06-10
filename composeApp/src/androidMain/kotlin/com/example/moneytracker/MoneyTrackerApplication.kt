package com.example.moneytracker

import android.app.Application
import com.example.moneytracker.data.FirebaseConfig

class MoneyTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseConfig.initialize(this)
    }
} 