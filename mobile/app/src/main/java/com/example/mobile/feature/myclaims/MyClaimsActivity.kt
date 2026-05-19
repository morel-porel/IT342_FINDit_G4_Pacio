package com.example.mobile.feature.myclaims

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.shared.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * MOB-06 — My Claims
 *
 * Vertical list of the authenticated user's claims,
 * each showing item name, submitted date, and status badge.
 */
class MyClaimsActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var rvMyClaims: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: MyClaimsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_claims)

        ivBack = findViewById(R.id.ivBack)
        rvMyClaims = findViewById(R.id.rvMyClaims)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)

        ivBack.setOnClickListener { finish() }

        adapter = MyClaimsAdapter(this, emptyList())
        rvMyClaims.layoutManager = LinearLayoutManager(this)
        rvMyClaims.adapter = adapter

        loadMyClaims()
    }

    private fun loadMyClaims() {
        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getClaims("Bearer $token")
                if (response.isSuccessful) {
                    val claims = response.body() ?: emptyList()
                    adapter.updateClaims(claims)
                    tvEmpty.visibility = if (claims.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    Toast.makeText(this@MyClaimsActivity, "Failed to load claims", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyClaimsActivity, "Connection error", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
