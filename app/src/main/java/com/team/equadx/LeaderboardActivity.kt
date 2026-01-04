package com.team.equadx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var leaderboardRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        leaderboardRecycler = findViewById(R.id.leaderboardRecycler)
        leaderboardRecycler.layoutManager = LinearLayoutManager(this)

        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { snapshot ->

                val list = snapshot.documents.map {
                    LeaderboardUser(
                        name = it.getString("fullName") ?: "User",
                        branch = it.getString("branch") ?: "Dept",
                        points = it.getLong("points") ?: 0
                    )
                }

                leaderboardRecycler.adapter = LeaderboardAdapter(list)
            }
    }
}
