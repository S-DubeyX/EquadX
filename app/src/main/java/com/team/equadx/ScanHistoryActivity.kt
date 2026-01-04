package com.team.equadx

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ScanHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val historyList = mutableListOf<ScanHistoryModel>()
    private lateinit var adapter: ScanHistoryAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_history)
        supportActionBar?.hide()

        recyclerView = findViewById(R.id.scanHistoryRecycler)
        adapter = ScanHistoryAdapter(historyList)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        listenToScanHistory()
    }

    private fun listenToScanHistory() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        listener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("scans")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, error ->

                if (error != null) return@addSnapshotListener

                historyList.clear()

                snapshots?.forEach { doc ->
                    historyList.add(
                        ScanHistoryModel(
                            binId = doc.id,
                            date = doc.getString("lastScanDate") ?: "",
                            points = doc.getLong("points") ?: 0
                        )
                    )
                }

                historyList.reverse() // latest first
                adapter.notifyDataSetChanged()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}
