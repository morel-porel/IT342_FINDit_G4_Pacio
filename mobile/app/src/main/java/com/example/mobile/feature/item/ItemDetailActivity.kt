package com.example.mobile.feature.item

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.feature.claim.ClaimSubmissionActivity
import com.example.mobile.shared.network.RetrofitClient
import kotlinx.coroutines.launch

/**
 * MOB-04 — Item Detail
 *
 * Displays full item info: image, type/status/category badges,
 * name, reporter, date, description, location, weather context card,
 * and Claim This Item button (hidden if item is not OPEN or user is reporter).
 */
class ItemDetailActivity : AppCompatActivity() {

    private lateinit var ivItemImage: ImageView
    private lateinit var tvTypeBadge: TextView
    private lateinit var tvStatusBadge: TextView
    private lateinit var tvCategoryBadge: TextView
    private lateinit var tvItemName: TextView
    private lateinit var tvReportedBy: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvLocation: TextView
    private lateinit var llWeatherCard: LinearLayout
    private lateinit var tvWeatherContext: TextView
    private lateinit var btnClaimItem: Button
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        ivItemImage = findViewById(R.id.ivItemImage)
        tvTypeBadge = findViewById(R.id.tvTypeBadge)
        tvStatusBadge = findViewById(R.id.tvStatusBadge)
        tvCategoryBadge = findViewById(R.id.tvCategoryBadge)
        tvItemName = findViewById(R.id.tvItemName)
        tvReportedBy = findViewById(R.id.tvReportedBy)
        tvDate = findViewById(R.id.tvDate)
        tvDescription = findViewById(R.id.tvDescription)
        tvLocation = findViewById(R.id.tvLocation)
        llWeatherCard = findViewById(R.id.llWeatherCard)
        tvWeatherContext = findViewById(R.id.tvWeatherContext)
        btnClaimItem = findViewById(R.id.btnClaimItem)
        btnBack = findViewById(R.id.ivBack)

        btnBack.setOnClickListener { finish() }

        val itemId = intent.getLongExtra("item_id", -1L)
        if (itemId == -1L) {
            Toast.makeText(this, "Item not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadItem(itemId)
    }

    private fun loadItem(itemId: Long) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getItemById(itemId)
                if (response.isSuccessful) {
                    val item = response.body() ?: return@launch

                    // Image
                    if (!item.imageUrl.isNullOrBlank()) {
                        val url = if (item.imageUrl.startsWith("http")) item.imageUrl
                                  else "http://10.0.2.2:8080/${item.imageUrl}"
                        Glide.with(this@ItemDetailActivity)
                            .load(url)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .centerCrop()
                            .into(ivItemImage)
                    }

                    // Type badge
                    tvTypeBadge.text = item.type
                    if (item.type == "FOUND") {
                        tvTypeBadge.setBackgroundColor(Color.parseColor("#DBEAFE"))
                        tvTypeBadge.setTextColor(Color.parseColor("#1D4ED8"))
                    } else {
                        tvTypeBadge.setBackgroundColor(Color.parseColor("#FEF3C7"))
                        tvTypeBadge.setTextColor(Color.parseColor("#D97706"))
                    }

                    // Status badge
                    tvStatusBadge.text = item.status
                    val (bgColor, txtColor) = when (item.status) {
                        "OPEN" -> "#DCFCE7" to "#16A34A"
                        "PENDING" -> "#FEF3C7" to "#D97706"
                        "RESOLVED", "APPROVED" -> "#F1F5F9" to "#64748B"
                        "REJECTED" -> "#FEE2E2" to "#DC2626"
                        else -> "#F1F5F9" to "#64748B"
                    }
                    tvStatusBadge.setBackgroundColor(Color.parseColor(bgColor))
                    tvStatusBadge.setTextColor(Color.parseColor(txtColor))

                    tvCategoryBadge.text = item.category
                    tvItemName.text = item.name
                    tvReportedBy.text = "Reported by ${item.reporter.fullName}"
                    tvDate.text = item.dateLostFound.take(10)
                    tvDescription.text = item.description ?: "No description provided."
                    tvLocation.text = item.location

                    // Weather context — only show if available
                    if (!item.weatherContext.isNullOrBlank()) {
                        llWeatherCard.visibility = View.VISIBLE
                        tvWeatherContext.text = item.weatherContext
                    } else {
                        llWeatherCard.visibility = View.GONE
                    }

                    // Claim button — only for OPEN found items
                    val prefs = getSharedPreferences("findit_prefs", MODE_PRIVATE)
                    val myUserId = prefs.getLong("userId", -1L)

                    if (item.type == "FOUND" && item.status == "OPEN" && item.reporter.id != myUserId) {
                        btnClaimItem.visibility = View.VISIBLE
                        btnClaimItem.setOnClickListener {
                            val intent = Intent(this@ItemDetailActivity, ClaimSubmissionActivity::class.java)
                            intent.putExtra("item_id", item.id)
                            intent.putExtra("item_name", item.name)
                            intent.putExtra("item_location", item.location)
                            intent.putExtra("item_date", item.dateLostFound.take(10))
                            intent.putExtra("item_image_url", item.imageUrl ?: "")
                            startActivity(intent)
                        }
                    } else {
                        btnClaimItem.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this@ItemDetailActivity, "Failed to load item", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ItemDetailActivity, "Connection error", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
