package com.example.belarusuniversities

import android.app.Application
import com.example.belarusuniversities.data.DatabaseInitializer

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DatabaseInitializer.initialize(this)
    }
}