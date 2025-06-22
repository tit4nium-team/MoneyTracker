package com.example.moneytracker

import android.app.Application
import com.example.moneytracker.data.FirebaseConfig
import com.example.moneytracker.service.initializePlatformManager // Mudança aqui

class MoneyTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseConfig.initialize(this)
        initializePlatformManager() // Mudança aqui
    }
} 