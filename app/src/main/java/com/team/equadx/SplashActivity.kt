package com.team.equadx

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, AuthGateActivity::class.java))
        finish()

//        val user = FirebaseAuth.getInstance().currentUser

//        if (user != null) {
//            startActivity(Intent(this, StudentDashboardActivity::class.java))
//        } else {
//            startActivity(Intent(this, LoginActivity::class.java))
//        }

        finish()
    }
}
