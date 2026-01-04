package com.team.equadx

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminScanHistoryActivity : AppCompatActivity() {

    private val scanList = mutableListOf<AdminScanModel>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminScanHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_scan_history)
        supportActionBar?.hide()

        recyclerView = findViewById(R.id.adminScanRecycler)
        adapter = AdminScanHistoryAdapter(scanList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadAllScans()
    }

    private fun loadAllScans() {

        val db = FirebaseFirestore.getInstance()

        db.collection("users").get().addOnSuccessListener { users ->

            scanList.clear()

            for (user in users) {
                val uid = user.id

                db.collection("users")
                    .document(uid)
                    .collection("scans")
                    .get()
                    .addOnSuccessListener { scans ->

                        for (scan in scans) {
                            scanList.add(
                                AdminScanModel(
                                    userId = uid,
                                    binId = scan.id,
                                    date = scan.getString("lastScanDate") ?: "",
                                    points = scan.getLong("points") ?: 0
                                )
                            )
                        }

                        adapter.notifyDataSetChanged()
                    }
            }
        }
    }
}
