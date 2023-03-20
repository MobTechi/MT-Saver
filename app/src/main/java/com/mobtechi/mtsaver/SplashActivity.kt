package com.mobtechi.mtsaver

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val threeSecs = 2000L
        Handler().postDelayed({
            // hide the splash screen and init the app
            val splashIntent = Intent(this, AppActivity::class.java)
            startActivity(splashIntent)
            finish()
        }, threeSecs)
    }
}