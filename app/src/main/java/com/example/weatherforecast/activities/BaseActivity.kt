package com.example.weatherforecast.activities
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.weatherforecast.WeatherApplication
import com.example.weatherforecast.interfaces.OnLocationRecieved
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.kotak.neobanking.interfaces.IOnActivityHandler


open class BaseActivity : AppCompatActivity() {
    private var activityResultContract: ActivityResultLauncher<Intent>? = null
    private lateinit var iOnActivityHandler: IOnActivityHandler
    private val isContinue = false
    private var isGPS = false
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    private val permissionResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle Permission granted/rejected
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    if (isGpsEnabled(this@BaseActivity)) {
                        getLocation(null)
                    }
                } else {
                    Toast.makeText(this@BaseActivity, "permission Denied $permissionName", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val gpsResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            getLocation(null)
        } else {
            Toast.makeText(this@BaseActivity, "GPS Not Enabled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResultContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            iOnActivityHandler.onActivtyCLosed(it)
        }
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            getPermission()
//        } else {
//            getLocation()
//        }
    }

    open fun launchActivityForResult(iOnWebviewOpen: IOnActivityHandler, intent: Intent) {
        activityResultContract?.launch(intent)
        this.iOnActivityHandler = iOnWebviewOpen
    }





    public fun getPermission() {
        permissionResultLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    fun isGpsEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun getLocation(onLocationRecieved: OnLocationRecieved?) {
        // checking location permission
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            getPermission()
        }
        if (!isGpsEnabled(this@BaseActivity)) {
            enableGPS(this@BaseActivity)
        }

        mFusedLocationClient?.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY,null)
            ?.addOnSuccessListener { location ->
                WeatherApplication.location = location
                onLocationRecieved?.onLocationStatus(true)
                Log.d("Device location","${WeatherApplication.location?.latitude} ${WeatherApplication.location?.longitude}")
            }
            ?.addOnFailureListener {
                onLocationRecieved?.onLocationStatus(false)
                Toast.makeText(
                    this, "Failed on getting current location",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    fun enableGPS(context: Context) {

        val mLocationRequest = LocationRequest.create()
            .setInterval(2000)
            .setFastestInterval(1000)

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)
        settingsBuilder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(context).checkLocationSettings(settingsBuilder.build())
        result.addOnCompleteListener { task ->

            //getting the status code from exception
            try {
                task.getResult(ApiException::class.java)
            } catch (ex: ApiException) {

                when (ex.statusCode) {

                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = ex as ResolvableApiException

                        val intentSenderRequest = IntentSenderRequest.Builder(resolvableApiException.resolution).build()
                        gpsResultLauncher.launch(intentSenderRequest)
                    } catch (e: IntentSender.SendIntentException) {
                        Toast.makeText(context, "PendingIntent unable to execute request.", Toast.LENGTH_SHORT).show()
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        Toast.makeText(
                            context,
                            "Something is wrong in your GPS",
                            Toast.LENGTH_SHORT
                        ).show()

                    }


                }
            }


        }


    }

}
