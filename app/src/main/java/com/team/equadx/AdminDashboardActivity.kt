package com.team.equadx

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)
        supportActionBar?.hide()

        // Generate QR
        findViewById<View>(R.id.btnGenerateQr).setOnClickListener {
            startActivity(Intent(this, AdminGenerateQrActivity::class.java))
        }

        // Scan History
        findViewById<View>(R.id.btnScanHistory).setOnClickListener {
            startActivity(Intent(this, AdminScanHistoryActivity::class.java))
        }

        // 🔴 LOGOUT
        findViewById<View>(R.id.btnLogout).setOnClickListener {
            logoutAdmin()
        }
    }

    private fun logoutAdmin() {
        FirebaseAuth.getInstance().signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}
