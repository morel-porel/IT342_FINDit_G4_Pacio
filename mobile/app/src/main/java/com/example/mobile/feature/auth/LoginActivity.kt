package com.example.mobile.feature.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobile.feature.dashboard.DashboardActivity
import com.example.mobile.R
import com.example.mobile.feature.auth.model.LoginRequest
import com.example.mobile.shared.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Logging in..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.login(
                        LoginRequest(email, password)
                    )
                    if (response.isSuccessful) {
                        val authResponse = response.body()
                        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putString("token", authResponse?.token)
                            .putString("fullName", authResponse?.user?.fullName)
                            .putString("email", authResponse?.user?.email)
                            .putString("role", authResponse?.user?.role)
                            .apply()

                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome back, ${authResponse?.user?.fullName?.split(" ")?.first()}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid email or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Connection error. Check your network.",
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                }
            }
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
