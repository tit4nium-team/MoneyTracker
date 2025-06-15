package com.example.moneytracker

import android.app.Application
import com.example.moneytracker.data.FirebaseConfig
import com.example.moneytracker.service.initializeGeminiService

class MoneyTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseConfig.initialize(this)
        initializeGeminiService()
    }
} 