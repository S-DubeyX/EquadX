package com.team.equadx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminScanHistoryAdapter(
    private val list: List<AdminScanModel>
) : RecyclerView.Adapter<AdminScanHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userText: TextView = view.findViewById(R.id.userText)
        val binText: TextView = view.findViewById(R.id.binText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val pointsText: TextView = view.findViewById(R.id.pointsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_scan_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.userText.text = "User: ${item.userId}"
        holder.binText.text = "Bin: ${item.binId}"
        holder.dateText.text = "Date: ${item.date}"
        holder.pointsText.text = "+${item.points} coins"
    }

    override fun getItemCount(): Int = list.size
}
