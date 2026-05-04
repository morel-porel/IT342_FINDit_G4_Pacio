package com.example.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mobile.model.RegisterRequest
import com.example.mobile.network.RetrofitClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (fullName.isEmpty()) {
                etFullName.error = "Full name is required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Enter a valid email"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                return@setOnClickListener
            }
            if (password.length < 8) {
                etPassword.error = "Password must be at least 8 characters"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            btnRegister.isEnabled = false
            btnRegister.text = "Creating account..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.register(
                        RegisterRequest(fullName, email, password, confirmPassword)
                    )
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration successful! Please log in.",
                            Toast.LENGTH_LONG
                        ).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        val errorMsg = when (response.code()) {
                            409 -> "Email already registered"
                            400 -> "Invalid input. Please check your details."
                            else -> "Registration failed. Please try again."
                        }
                        Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Connection error. Check your network.",
                        Toast.LENGTH_SHORT
                    ).show()
                } finally {
                    btnRegister.isEnabled = true
                    btnRegister.text = "Create Account"
                }
            }
        }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}