package com.example.mobile.feature.myclaims

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.feature.claim.model.ClaimResponse

class MyClaimsAdapter(
    private val context: Context,
    private var claims: List<ClaimResponse>
) : RecyclerView.Adapter<MyClaimsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvItemName: TextView = view.findViewById(R.id.tvItemName)
        val tvItemLocation: TextView = view.findViewById(R.id.tvItemLocation)
        val tvSubmittedDate: TextView = view.findViewById(R.id.tvSubmittedDate)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_claim_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val claim = claims[position]

        holder.tvItemName.text = claim.itemName
        holder.tvItemLocation.text = claim.itemLocation
        holder.tvSubmittedDate.text = "Submitted: ${claim.createdAt.take(10)}"

        holder.tvStatusBadge.text = claim.status
        val (bgColor, txtColor) = when (claim.status) {
            "PENDING" -> "#FEF3C7" to "#D97706"
            "APPROVED" -> "#DCFCE7" to "#16A34A"
            "REJECTED" -> "#FEE2E2" to "#DC2626"
            else -> "#F1F5F9" to "#64748B"
        }
        holder.tvStatusBadge.setBackgroundColor(Color.parseColor(bgColor))
        holder.tvStatusBadge.setTextColor(Color.parseColor(txtColor))
    }

    override fun getItemCount(): Int = claims.size

    fun updateClaims(newClaims: List<ClaimResponse>) {
        claims = newClaims
        notifyDataSetChanged()
    }
}
