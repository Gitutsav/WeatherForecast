package com.example.weatherforecast

import android.app.Application
import android.content.Context
import android.location.Location

class WeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit  var appContext: Context
        var location: Location? = null
    }
}