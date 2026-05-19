package com.example.mobile.feature.dashboard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.mobile.R
import com.example.mobile.feature.item.ItemAdapter
import com.example.mobile.feature.item.ReportItemActivity
import com.example.mobile.feature.item.model.ItemResponse
import com.example.mobile.feature.profile.ProfileActivity
import com.example.mobile.shared.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * MOB-03 — Home Feed
 *
 * Top bar with FINDit logo, avatar, horizontally scrollable filter chips,
 * RecyclerView of item cards, FAB for quick report,
 * pull-to-refresh, and background polling every 10 seconds.
 */
class DashboardActivity : AppCompatActivity() {

    private lateinit var tvAvatar: TextView
    private lateinit var rvItems: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var tvEmpty: TextView
    private lateinit var fabReport: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var chipAll: TextView
    private lateinit var chipLost: TextView
    private lateinit var chipFound: TextView

    private lateinit var adapter: ItemAdapter
    private val pollHandler = Handler(Looper.getMainLooper())
    private val pollInterval = 10_000L
    private var activeFilter = "ALL"

    private val pollRunnable = object : Runnable {
        override fun run() {
            loadItems(silent = true)
            pollHandler.postDelayed(this, pollInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        tvAvatar = findViewById(R.id.tvAvatar)
        rvItems = findViewById(R.id.rvItems)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        tvEmpty = findViewById(R.id.tvEmpty)
        fabReport = findViewById(R.id.fabReport)
        chipAll = findViewById(R.id.chipAll)
        chipLost = findViewById(R.id.chipLost)
        chipFound = findViewById(R.id.chipFound)

        // Avatar initials
        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
        val fullName = prefs.getString("fullName", "?") ?: "?"
        tvAvatar.text = fullName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("").uppercase()

        tvAvatar.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // RecyclerView
        adapter = ItemAdapter(this, emptyList())
        rvItems.layoutManager = LinearLayoutManager(this)
        rvItems.adapter = adapter

        // Swipe-to-refresh
        swipeRefresh.setOnRefreshListener { loadItems(silent = false) }

        // FAB → Report Item
        fabReport.setOnClickListener {
            startActivity(Intent(this, ReportItemActivity::class.java))
        }

        // Filter chips
        setupFilterChips()

        loadItems(silent = false)
    }

    override fun onStart() {
        super.onStart()
        pollHandler.postDelayed(pollRunnable, pollInterval)
    }

    override fun onStop() {
        super.onStop()
        pollHandler.removeCallbacks(pollRunnable)
    }

    // Reload items when returning from detail/report
    override fun onResume() {
        super.onResume()
        loadItems(silent = true)
    }

    private fun setupFilterChips() {
        val chips = listOf(chipAll, chipLost, chipFound)
        val labels = listOf("ALL", "LOST", "FOUND")

        chips.forEachIndexed { i, chip ->
            chip.setOnClickListener {
                activeFilter = labels[i]
                chips.forEach { c ->
                    c.setBackgroundColor(Color.parseColor("#E2E8F0"))
                    c.setTextColor(Color.parseColor("#334155"))
                }
                chip.setBackgroundColor(Color.parseColor("#1D4ED8"))
                chip.setTextColor(Color.WHITE)
                loadItems(silent = false)
            }
        }
        chipAll.setBackgroundColor(Color.parseColor("#1D4ED8"))
        chipAll.setTextColor(Color.WHITE)
    }

    private fun loadItems(silent: Boolean) {
        val typeFilter = when (activeFilter) {
            "LOST" -> "LOST"
            "FOUND" -> "FOUND"
            else -> null
        }

        if (!silent) swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getItems(type = typeFilter, status = "OPEN")
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    adapter.updateItems(items)
                    tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
            } catch (_: Exception) {
                // Silent fail on background poll
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }
}
