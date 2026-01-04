package com.team.equadx

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(
    private val users: List<LeaderboardUser>
) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: LinearLayout = view.findViewById(R.id.rootCard)
        val rank: TextView = view.findViewById(R.id.rankText)
        val name: TextView = view.findViewById(R.id.nameText)
        val branch: TextView = view.findViewById(R.id.branchText)
        val points: TextView = view.findViewById(R.id.pointsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        val rank = position + 1

        holder.rank.text = "$rank."
        holder.name.text = user.name
        holder.branch.text = "${user.branch} Dept"
        holder.points.text = user.points.toString()

        // Highlight top rank
        if (rank == 1) {
            holder.root.setBackgroundColor(Color.parseColor("#FFF8E1"))
        } else {
            holder.root.setBackgroundColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int = users.size
}
