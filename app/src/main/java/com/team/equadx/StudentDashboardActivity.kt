package com.team.equadx

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentDashboardActivity : AppCompatActivity() {

    private lateinit var walletText: TextView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val qrLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val reward = result.data?.getLongExtra("QR_POINTS", 0) ?: 0
                if (reward > 0) {
                    showSuccessDialog(reward)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_student_dashboard)

        walletText = findViewById(R.id.walletText)

        val leaderboardCard = findViewById<View>(R.id.cardLeaderboard)
        val reportCard = findViewById<View>(R.id.cardReport)
        val cardMarketplace = findViewById<View>(R.id.cardMarketplace)

        leaderboardCard.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.card_press)
            )

            it.postDelayed({
                startActivity(
                    Intent(this, LeaderboardActivity::class.java)
                )
            }, 120)
        }

        reportCard.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.card_press)
            )

            it.postDelayed({
                startActivity(
                    Intent(this, ReportIssueActivity::class.java)
                )
            }, 120)
        }

        cardMarketplace.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.card_press)
            )

            it.postDelayed({
                startActivity(
                    Intent(this, MapBinsActivity::class.java)
                )
            }, 120)
        }

        findViewById<View>(R.id.btnProfileBottom).setOnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
        }




        observeWallet()
        setupScanButton()
        setupBottomScanButton()
    }

    private fun setupScanButton() {
        val scanButton = findViewById<View>(R.id.scanButton)

        scanButton.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.card_press)
            )

            qrLauncher.launch(
                android.content.Intent(this, QrScannerActivity::class.java)
            )
        }
    }

    private fun setupBottomScanButton() {
        val BottomscanButton = findViewById<View>(R.id.bottomQrScanner)

        BottomscanButton.setOnClickListener {
            it.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.card_press)
            )

            qrLauncher.launch(
                android.content.Intent(this, QrScannerActivity::class.java)
            )
        }
    }

    // 🔥 REALTIME WALLET
    private fun observeWallet() {

        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .addSnapshotListener { snapshot, _ ->

                val points = snapshot?.getLong("points") ?: 0
                walletText.text = "$points Coins"
            }
    }

    private fun showSuccessDialog(points: Long) {
        AlertDialog.Builder(this)
            .setTitle("Scan Successful 🎉")
            .setMessage("You earned $points coins!")
            .setPositiveButton("OK", null)
            .show()
    }
}
