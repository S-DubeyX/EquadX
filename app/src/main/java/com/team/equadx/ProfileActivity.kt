package com.team.equadx

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvIdDept: TextView
    private lateinit var tvCoins: TextView
    private lateinit var editProfile: TextView

    private lateinit var recycleHistory: TextView


    private var profileListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        tvName = findViewById(R.id.tvName)
        tvIdDept = findViewById(R.id.tvIdDept)
        tvCoins = findViewById(R.id.tvCoins)
        editProfile = findViewById(R.id.editProfile)
        recycleHistory = findViewById(R.id.recycleHistory)

        startRealtimeProfileListener()

        findViewById<TextView>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(this, LoginActivity::class.java)
            )
            finishAffinity()
        }

        findViewById<TextView>(R.id.editProfile).setOnClickListener {
            startActivity(
                Intent(this, EditProfileActivity::class.java)
            )
            finishAffinity()
        }

        findViewById<TextView>(R.id.recycleHistory).setOnClickListener {
            startActivity(
                Intent(this, ScanHistoryActivity::class.java)
            )
            finishAffinity()
        }

    }

    private fun startRealtimeProfileListener() {

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .addSnapshotListener { document, error ->

                if (error != null) {
                    Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (document == null || !document.exists()) {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                // 🔍 LOG ALL DATA (IMPORTANT)
                val data = document.data
                println("PROFILE DATA = $data")

                val name = document.getString("fullName") ?: "User"
                val studentId = document.getString("studentId") ?: "N/A"
                val branch = document.getString("branch") ?: "N/A"
                val coins = document.getLong("points") ?: 0L

                tvName.text = name
                tvIdDept.text = "ID: $studentId | $branch Dept"
                tvCoins.text = coins.toString()
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        profileListener?.remove()
    }
}
