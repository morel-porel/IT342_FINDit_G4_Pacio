package com.example.mobile.feature.myreports

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.feature.item.ItemDetailActivity
import com.example.mobile.feature.item.model.ItemResponse
import com.example.mobile.shared.network.RetrofitClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

/**
 * MOB-08 — My Reports
 *
 * Shows the authenticated user's own items with filter chips.
 * Each card has Edit (→ detail) and Resolve buttons.
 * Resolve shows a bottom sheet confirmation.
 */
class MyReportsActivity : AppCompatActivity() {

    private lateinit var rvMyReports: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var ivBack: ImageView
    private lateinit var filterChipAll: TextView
    private lateinit var filterChipLost: TextView
    private lateinit var filterChipFound: TextView
    private lateinit var filterChipOpen: TextView
    private lateinit var filterChipResolved: TextView

    private var allItems: List<ItemResponse> = emptyList()
    private var activeFilter = "ALL"
    private lateinit var adapter: MyReportsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reports)

        ivBack = findViewById(R.id.ivBack)
        rvMyReports = findViewById(R.id.rvMyReports)
        tvEmpty = findViewById(R.id.tvEmpty)
        progressBar = findViewById(R.id.progressBar)
        filterChipAll = findViewById(R.id.chipAll)
        filterChipLost = findViewById(R.id.chipLost)
        filterChipFound = findViewById(R.id.chipFound)
        filterChipOpen = findViewById(R.id.chipOpen)
        filterChipResolved = findViewById(R.id.chipResolved)

        ivBack.setOnClickListener { finish() }

        adapter = MyReportsAdapter(
            context = this,
            items = emptyList(),
            onResolve = { item -> showResolveBottomSheet(item) }
        )
        rvMyReports.layoutManager = LinearLayoutManager(this)
        rvMyReports.adapter = adapter

        setupFilterChips()
        loadMyReports()
    }

    private fun setupFilterChips() {
        val chips = listOf(filterChipAll, filterChipLost, filterChipFound,
                           filterChipOpen, filterChipResolved)
        val labels = listOf("ALL", "LOST", "FOUND", "OPEN", "RESOLVED")

        chips.forEachIndexed { i, chip ->
            chip.setOnClickListener {
                activeFilter = labels[i]
                chips.forEach { c ->
                    c.setBackgroundColor(Color.parseColor("#E2E8F0"))
                    c.setTextColor(Color.parseColor("#334155"))
                }
                chip.setBackgroundColor(Color.parseColor("#1D4ED8"))
                chip.setTextColor(Color.WHITE)
                applyFilter()
            }
        }
        // Default selection
        filterChipAll.setBackgroundColor(Color.parseColor("#1D4ED8"))
        filterChipAll.setTextColor(Color.WHITE)
    }

    private fun applyFilter() {
        val filtered = when (activeFilter) {
            "LOST" -> allItems.filter { it.type == "LOST" }
            "FOUND" -> allItems.filter { it.type == "FOUND" }
            "OPEN" -> allItems.filter { it.status == "OPEN" }
            "RESOLVED" -> allItems.filter { it.status == "RESOLVED" }
            else -> allItems
        }
        adapter.updateItems(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadMyReports() {
        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMyItems("Bearer $token")
                if (response.isSuccessful) {
                    allItems = response.body() ?: emptyList()
                    applyFilter()
                } else {
                    Toast.makeText(this@MyReportsActivity, "Failed to load reports", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyReportsActivity, "Connection error", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun showResolveBottomSheet(item: ItemResponse) {
        val bottomSheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_resolve, null)
        bottomSheet.setContentView(view)

        view.findViewById<TextView>(R.id.tvResolveItemName).text = item.name
        view.findViewById<Button>(R.id.btnConfirmResolve).setOnClickListener {
            bottomSheet.dismiss()
            resolveItem(item)
        }
        view.findViewById<Button>(R.id.btnCancelResolve).setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    private fun resolveItem(item: ItemResponse) {
        val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
        val token = prefs.getString("token", null) ?: return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.resolveItem("Bearer $token", item.id)
                if (response.isSuccessful) {
                    Toast.makeText(this@MyReportsActivity, "${item.name} marked as resolved", Toast.LENGTH_SHORT).show()
                    loadMyReports()
                } else {
                    Toast.makeText(this@MyReportsActivity, "Failed to resolve item", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyReportsActivity, "Connection error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
