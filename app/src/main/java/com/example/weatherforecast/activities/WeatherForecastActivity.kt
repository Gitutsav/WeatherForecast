package com.example.weatherforecast.activities

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherforecast.BaseUtils
import com.example.weatherforecast.R
import com.example.weatherforecast.WeatherApplication
import com.example.weatherforecast.adapters.DailyAdapter
import com.example.weatherforecast.adapters.HoursAdapter
import com.example.weatherforecast.databinding.ActivityMainBinding
import com.example.weatherforecast.databinding.ActivityPermissionsBinding
import com.example.weatherforecast.models.TemperatureItem
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kotak.neobanking.extensions.castJson
import com.kotak.neobanking.extensions.getInJson
import com.kotak.neobanking.network.CustomCallAdapter
import com.kotak.neobanking.network.ErrorCode
import com.kotak.neobanking.network.NetworkClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WeatherForecastActivity : BaseActivity() {
    private var location: Location? = null
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)

        getData()

        binding.tvProceed.setOnClickListener {
           it.visibility = View.GONE
           getData()
        }
    }
    fun getData(){
        if (checkConditions()) {
            location = WeatherApplication.location
            binding.tvLocation.text = "Location - ${String.format("%.2f", location?.latitude).toDouble()}, ${String.format("%.2f", location?.longitude).toDouble()}"
            getHourlyTemperature()
            getDailyTemperature()
        } else{
            finish()
            startActivity(Intent(this,PermissionsActivity::class.java))
        }
    }

    fun getHourlyTemperature(){
        binding.rvHourly.visibility = View.GONE
        binding.pbHours.visibility = View.VISIBLE
        val url = "https://api.open-meteo.com/v1/forecast?latitude=${location?.latitude}&longitude=${location?.longitude}&hourly=temperature_2m"
        var requestCall: CustomCallAdapter.MyCall<ResponseBody>? = null
        requestCall = NetworkClass.getInstance(this, NetworkClass.HttpClient.Internal).getRequest(url, HashMap())
        val customCallAdapter = object : CustomCallAdapter.MyCallback<ResponseBody> {
            override fun success(response: Response<ResponseBody>) {
                CoroutineScope(Dispatchers.Main).launch {

                    binding.pbHours.visibility = View.INVISIBLE
                    val bodyString = response.body()?.string()
                    val calendar: Calendar = Calendar.getInstance()
                    val hour24hrs: Int = calendar.get(Calendar.HOUR_OF_DAY)
                    val requiredRange = hour24hrs + 6
                    val responseJson = BaseUtils.getJsonFromString(bodyString)
                    val hours = responseJson.getInJson<JsonObject>("hourly")
                    val hoursArray = hours.getInJson<JsonArray>("time")
                    val temperatureArray = hours.getInJson<JsonArray>("temperature_2m")
                    binding.tvTemperature.text = temperatureArray?.get(hour24hrs).castJson<Double>().toString() + " \u2103"

                    if (hoursArray != null && temperatureArray != null) {
                        var dataList = ArrayList<TemperatureItem>()
                        for (i in hour24hrs..requiredRange) {
                            dataList.add(TemperatureItem((i % 24).toString() + " Hrs", temperatureArray[i].castJson<Double>().toString()+" \u2103"))
                        }
                        val customAdapter = HoursAdapter(this@WeatherForecastActivity, dataList)
                        binding.rvHourly.visibility = View.VISIBLE
                        binding.rvHourly.layoutManager = LinearLayoutManager(this@WeatherForecastActivity, LinearLayoutManager.HORIZONTAL, false)
                        binding.rvHourly.adapter = customAdapter

                        Log.i("hours", dataList.toString())
                    }
                }

            }

            override fun failure(t: Throwable?, errorCode: ErrorCode, response: Response<ResponseBody>?) {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.pbHours.visibility = View.INVISIBLE
                    binding.tvProceed.visibility = View.VISIBLE
                    val errorObjectList = ArrayList<Pair<String, String?>>()
                    errorObjectList.add(Pair("response", response?.errorBody()?.string() ?: ""))
                    errorObjectList.add(Pair("errorcode", "${response?.code()} : ${errorCode.name}"))
                    errorObjectList.add(Pair("cause", t?.message ?: ""))
                }
            }
        }
        requestCall.enqueue(customCallAdapter)
    }

     fun getDailyTemperature(){
         binding.pbDaily.visibility = View.VISIBLE
         binding.rvDaily.visibility = View.GONE
         val url = "https://api.open-meteo.com/v1/forecast?latitude=${location?.latitude}&longitude=${location?.longitude}&daily=temperature_2m_max&timezone=GMT"
        var requestCall: CustomCallAdapter.MyCall<ResponseBody>? = null
        requestCall = NetworkClass.getInstance(this, NetworkClass.HttpClient.Internal).getRequest(url, HashMap())
        val customCallAdapter = object : CustomCallAdapter.MyCallback<ResponseBody> {
            override fun success(response: Response<ResponseBody>) {

                CoroutineScope(Dispatchers.Main).launch {
                    binding.pbDaily.visibility = View.INVISIBLE
                    val bodyString = response.body()?.string()
                    val responseJson = BaseUtils.getJsonFromString(bodyString)
                    val daily = responseJson.getInJson<JsonObject>("daily")
                    val dailyArray = daily.getInJson<JsonArray>("time")
                    val temperatureArray = daily.getInJson<JsonArray>("temperature_2m_max")

                    if (dailyArray != null && temperatureArray != null) {
                        var dataList = ArrayList<TemperatureItem>()
                        for (i in 0 until dailyArray.size()) {
                            dataList.add(TemperatureItem(dailyArray[i].castJson<String>() ?: "", temperatureArray[i].castJson<Double>().toString()+" \u2103"))
                        }
                        val customAdapter = DailyAdapter(this@WeatherForecastActivity, dataList)
                        binding.rvDaily.visibility = View.VISIBLE
                        binding.rvDaily.layoutManager = LinearLayoutManager(this@WeatherForecastActivity)
                        binding.rvDaily.adapter = customAdapter
                        Log.i("daily", dataList.toString())

                    }
                }
            }

            override fun failure(t: Throwable?, errorCode: ErrorCode, response: Response<ResponseBody>?) {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.pbDaily.visibility = View.INVISIBLE
                    binding.tvProceed.visibility = View.VISIBLE
                    val errorObjectList = ArrayList<Pair<String, String?>>()
                    errorObjectList.add(Pair("response", response?.errorBody()?.string() ?: ""))
                    errorObjectList.add(Pair("errorcode", "${response?.code()} : ${errorCode.name}"))
                    errorObjectList.add(Pair("cause", t?.message ?: ""))
                }
            }
        }
        requestCall.enqueue(customCallAdapter)
    }
    fun checkConditions(): Boolean{
        val internetConnected = BaseUtils.isOnline(this)
        val coarseLocationPermissionGiven = BaseUtils.isPermissionGranted(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocationPermissionGiven = BaseUtils.isPermissionGranted(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val gpsEnabled = BaseUtils.isGpsEnabled(this)
        return internetConnected && coarseLocationPermissionGiven && fineLocationPermissionGiven && gpsEnabled
    }
}