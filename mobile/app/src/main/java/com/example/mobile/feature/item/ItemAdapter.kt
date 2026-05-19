package com.example.mobile.feature.item

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mobile.R
import com.example.mobile.feature.item.model.ItemResponse
import com.example.mobile.shared.network.RetrofitClient

class ItemAdapter(
    private val context: Context,
    private var items: List<ItemResponse>
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvLocation: TextView = view.findViewById(R.id.tvItemLocation)
        val tvDate: TextView = view.findViewById(R.id.tvItemDate)
        val tvTypeBadge: TextView = view.findViewById(R.id.tvTypeBadge)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivItemThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
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
            val imageUrl = buildImageUrl(item.imageUrl)
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.ivThumbnail)
        } else {
            holder.ivThumbnail.setImageResource(R.drawable.placeholder_image)
        }

        // Navigate to detail on tap
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

    private fun buildImageUrl(path: String): String {
        return if (path.startsWith("http")) path
        else "http://10.0.2.2:8080/$path"
    }
}
