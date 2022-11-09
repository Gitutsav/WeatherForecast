package com.example.weatherforecast.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.databinding.DataBindingUtil
import com.example.weatherforecast.BaseUtils
import com.example.weatherforecast.R
import com.example.weatherforecast.WeatherApplication
import com.example.weatherforecast.databinding.ActivityPermissionsBinding
import com.example.weatherforecast.interfaces.OnLocationRecieved


class PermissionsActivity : BaseActivity() {

    private lateinit var activityPermissionsBinding: ActivityPermissionsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityPermissionsBinding = DataBindingUtil.setContentView(this,R.layout.activity_permissions)

        activityPermissionsBinding.tvProceed.setOnClickListener {
            val conditionPair = checkConditions()
            if (conditionPair.first){
                if (WeatherApplication.location!=null){
                    finish()
                    startActivity(Intent(this@PermissionsActivity, WeatherForecastActivity::class.java))
                } else{
                    activityPermissionsBinding.pbLoader.visibility = View.VISIBLE
                    activityPermissionsBinding.tvProceed.isEnabled = false
                    activityPermissionsBinding.tvProceed.text = ""
                    getLocation(object : OnLocationRecieved{
                        override fun onLocationStatus(status: Boolean) {
                            activityPermissionsBinding.pbLoader.visibility = View.INVISIBLE
                            activityPermissionsBinding.tvProceed.isEnabled = true
                            activityPermissionsBinding.tvProceed.text = "Proceed"
                        }
                    })
                }
            } else{
                when(conditionPair.second){
                    Condition.Internet->{
                        startActivity( Intent(Settings.ACTION_WIFI_SETTINGS))
                    }
                    Condition.Location->{
                        getPermission()
                    }
                    Condition.GPS->{
                        enableGPS(this)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

//    override fun onStart() {
//        super.onStart()
//        updateUI()
//    }

    fun checkConditions(): Pair<Boolean,Condition>{
        val internetConnected = BaseUtils.isOnline(this)
        val coarseLocationPermissionGiven = BaseUtils.isPermissionGranted(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocationPermissionGiven = BaseUtils.isPermissionGranted(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val gpsEnabled = BaseUtils.isGpsEnabled(this)

        if (internetConnected){
            activityPermissionsBinding.ivInternetStatus.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_24))
        } else {
            activityPermissionsBinding.ivInternetStatus.setImageDrawable(getDrawable(R.drawable.ic_baseline_cancel_24))
            return Pair(false,Condition.Internet)
        }

        if (coarseLocationPermissionGiven && fineLocationPermissionGiven){
            activityPermissionsBinding.ivLocationStatus.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_24))
        } else{
            activityPermissionsBinding.ivLocationStatus.setImageDrawable(getDrawable(R.drawable.ic_baseline_cancel_24))
            return Pair(false,Condition.Location)
        }

        if (gpsEnabled){
            activityPermissionsBinding.ivGpsStatus.setImageDrawable(getDrawable(R.drawable.ic_baseline_check_24))
            return Pair(true,Condition.NONE)
        } else{
            activityPermissionsBinding.ivGpsStatus.setImageDrawable(getDrawable(R.drawable.ic_baseline_cancel_24))
            return Pair(false,Condition.GPS)
        }
    }

    fun updateUI(){
        val conditionPair = checkConditions()
        if (conditionPair.first){
            activityPermissionsBinding.tvProceed.text = "Fetch location"
        } else{
            when(conditionPair.second){
                Condition.Internet->{
                    activityPermissionsBinding.tvProceed.text = "Check internet connection"
                }
                Condition.Location->{
                    activityPermissionsBinding.tvProceed.text = "Get location permission"
                }
                Condition.GPS->{
                    activityPermissionsBinding.tvProceed.text = "Enable GPS"
                }
            }
        }
    }

    enum class Condition{
        Internet,
        Location,
        GPS,
        NONE
    }
}