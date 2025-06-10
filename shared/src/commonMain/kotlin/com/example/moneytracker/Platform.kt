package com.example.moneytracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform