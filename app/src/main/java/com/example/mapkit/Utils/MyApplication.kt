package com.example.mapkit.Utils

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey(Utils.MAPKIT_KEY)
    }
}