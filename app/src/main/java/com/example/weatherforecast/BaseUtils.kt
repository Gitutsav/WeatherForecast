package com.example.weatherforecast

import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.LocationManager
import android.location.LocationRequest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException

class BaseUtils {

    companion object {
        fun isOnline(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.activeNetwork
            } else {
                TODO("VERSION.SDK_INT < M")
            }
            val capabilities = connectivityManager
                .getNetworkCapabilities(network)
            return (capabilities != null
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        }

        fun isPermissionGranted(context: Context,permission: String?): Boolean {
            return ContextCompat.checkSelfPermission(context, permission!!) == PackageManager.PERMISSION_GRANTED
        }

        fun isGpsEnabled(context: Context): Boolean {
            var locationManager: LocationManager? = null
            val gpsEnabled: Boolean
            val networkEnabled: Boolean
            if (locationManager == null) {
                locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            }
            try {
                gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
                ex.printStackTrace()
                return false
            }
            return gpsEnabled && networkEnabled
        }
        /**
         *
         * @param jsonString - Stringified Json
         * @return - JsonElement for the given string if parsing is successfull else return null
         */
        fun getJsonFromString(jsonString: String?): JsonElement? {
            var value: JsonElement? = null
            try {
                value = JsonParser.parseString(jsonString)
            } catch (e: Exception) {
                when (e::class) {
                    JsonParseException::class -> {
                        Log.e("JsonParseError", e.message?:"")
                    }
                    JsonSyntaxException::class -> {
                        Log.e("JsonParseError", e.message?:"")
                    }
                    OutOfMemoryError::class -> {
                        Log.e("JsonParseError", e.message?:"")
                    }
                    MalformedJsonException::class -> {
                        Log.e("JsonParseError", e.message?:"")
                    }
                }
            }
            return value
        }

    }
}