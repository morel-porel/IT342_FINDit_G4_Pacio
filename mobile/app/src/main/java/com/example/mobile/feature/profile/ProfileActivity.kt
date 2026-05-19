package com.example.mobile.feature.profile

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.R
import com.example.mobile.feature.auth.LoginActivity
import com.example.mobile.feature.myclaims.MyClaimsActivity
import com.example.mobile.feature.myreports.MyReportsActivity
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * MOB-07 — Profile
 *
 * Avatar (initials), full name, email, role badge.
 * Account info section (read-only).
 * Menu: My Reports, My Claims, Log Out.
 * Log Out triggers MOB-10 bottom sheet confirmation.
 */
class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val role = prefs.getString("role", "USER") ?: "USER"

        // Avatar initials
        val initials = fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()

        findViewById<TextView>(R.id.tvAvatar).text = initials
        findViewById<TextView>(R.id.tvFullName).text = fullName
        findViewById<TextView>(R.id.tvEmail).text = email

        val tvRoleBadge = findViewById<TextView>(R.id.tvRoleBadge)
        tvRoleBadge.text = role
        if (role == "ADMIN") {
            tvRoleBadge.setBackgroundColor(android.graphics.Color.parseColor("#EDE9FE"))
            tvRoleBadge.setTextColor(android.graphics.Color.parseColor("#7C3AED"))
        }

        // Account info (read-only display)
        findViewById<TextView>(R.id.tvInfoFullName).text = fullName
        findViewById<TextView>(R.id.tvInfoEmail).text = email

        // Menu items
        findViewById<LinearLayout>(R.id.menuMyReports).setOnClickListener {
            startActivity(Intent(this, MyReportsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.menuMyClaims).setOnClickListener {
            startActivity(Intent(this, MyClaimsActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.menuLogOut).setOnClickListener {
            showLogoutBottomSheet()
        }
    }

    private fun showLogoutBottomSheet() {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_logout, null)
        bottomSheet.setContentView(view)

        view.findViewById<Button>(R.id.btnConfirmLogout).setOnClickListener {
            bottomSheet.dismiss()
            doLogout()
        }
        view.findViewById<Button>(R.id.btnCancelLogout).setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun doLogout() {
        // Clear stored JWT and user data
        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Navigate to login, clear back stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}
