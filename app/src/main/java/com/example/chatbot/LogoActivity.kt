package com.example.chatbot

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class LogoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logo)

        Handler(Looper.getMainLooper()).postDelayed({
            val imgView : ImageView = findViewById(R.id.imageView)
            imgView.setImageResource(R.drawable.logo) // Assuming your image is named 'logo.png' or 'logo.jpg'

            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()

        }, 1500) // 3 seconds
    }
}