package com.example.weatherforecast.activities

import android.content.Intent
import android.os.Bundle
import com.example.weatherforecast.R
import java.util.*
import kotlin.concurrent.schedule

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Timer().schedule(2000) {
            finish()
            startActivity(Intent(this@SplashActivity,PermissionsActivity::class.java))
        }
    }
}