package com.example.mobile.feature.myreports

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.feature.item.ItemDetailActivity
import com.example.mobile.feature.item.model.ItemResponse

class MyReportsAdapter(
    private val context: Context,
    private var items: List<ItemResponse>,
    private val onResolve: (ItemResponse) -> Unit
) : RecyclerView.Adapter<MyReportsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivItemThumbnail)
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvLocation: TextView = view.findViewById(R.id.tvItemLocation)
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
        val tvTypeBadge: TextView = view.findViewById(R.id.tvTypeBadge)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        val btnResolve: Button = view.findViewById(R.id.btnResolve)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_my_report_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvName.text = item.name
        holder.tvLocation.text = item.location
        holder.tvDate.text = item.dateLostFound.take(10)

        // Type badge
        holder.tvTypeBadge.text = item.type
        if (item.type == "FOUND") {
            holder.tvTypeBadge.setBackgroundColor(Color.parseColor("#DBEAFE"))
            holder.tvTypeBadge.setTextColor(Color.parseColor("#1D4ED8"))
        } else {
            holder.tvTypeBadge.setBackgroundColor(Color.parseColor("#FEF3C7"))
            holder.tvTypeBadge.setTextColor(Color.parseColor("#D97706"))
        }

        // Status badge
        holder.tvStatusBadge.text = item.status
        val (bgColor, txtColor) = when (item.status) {
            "OPEN" -> "#DCFCE7" to "#16A34A"
            "PENDING" -> "#FEF3C7" to "#D97706"
            "RESOLVED", "APPROVED" -> "#F1F5F9" to "#64748B"
            "REJECTED" -> "#FEE2E2" to "#DC2626"
            else -> "#F1F5F9" to "#64748B"
        }
        holder.tvStatusBadge.setBackgroundColor(Color.parseColor(bgColor))
        holder.tvStatusBadge.setTextColor(Color.parseColor(txtColor))

        // Thumbnail
        if (!item.imageUrl.isNullOrBlank()) {
            val url = if (item.imageUrl.startsWith("http")) item.imageUrl
                      else "http://10.0.2.2:8080/${item.imageUrl}"
            Glide.with(context).load(url)
                .placeholder(R.drawable.placeholder_image)
                .centerCrop().into(holder.ivThumbnail)
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.placeholder_image)
        }

        // Resolve button — only for OPEN items
        if (item.status == "OPEN") {
            holder.btnResolve.visibility = View.VISIBLE
            holder.btnResolve.setOnClickListener { onResolve(item) }
        } else {
            holder.btnResolve.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ItemDetailActivity::class.java)
            intent.putExtra("item_id", item.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ItemResponse>) {
        items = newItems
        notifyDataSetChanged()
    }
}
