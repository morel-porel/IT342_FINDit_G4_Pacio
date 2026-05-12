package com.example.mobile.feature.dashboard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.R

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "there") ?: "there"
        val firstName = fullName.split(" ").first()

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvAvatar = findViewById<TextView>(R.id.tvAvatar)

        tvWelcome.text = "Welcome back, $firstName!"
        tvAvatar.text = fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()
    }
}