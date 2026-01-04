package com.team.equadx

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.analytics.FirebaseAnalytics
import androidx.appcompat.app.AppCompatDelegate


class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button
    private lateinit var createAccount: TextView
    private lateinit var studentTab: TextView
    private lateinit var adminTab: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var analytics: FirebaseAnalytics

    private var selectedRole = "student" // default UI role

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

        setContentView(R.layout.activity_login) // or signup
        supportActionBar?.hide()
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        analytics = FirebaseAnalytics.getInstance(this)


        // 🔁 AUTO LOGIN (CRITICAL FIX)
        val currentUser = auth.currentUser
        if (currentUser != null) {
            redirectBasedOnRole(currentUser.uid)
            return
        }

        // Views
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)
        createAccount = findViewById(R.id.createAccount)
        studentTab = findViewById(R.id.studentTab)
        adminTab = findViewById(R.id.adminTab)

        // Default tab
        selectStudentTab()

        studentTab.setOnClickListener { selectStudentTab() }
        adminTab.setOnClickListener { selectAdminTab() }

        loginBtn.setOnClickListener {
            loginBtn.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.button_scale)
            )
            loginUser()
        }

        createAccount.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    // ---------------- ROLE UI ----------------

    private fun selectStudentTab() {
        selectedRole = "student"
        studentTab.setBackgroundResource(R.drawable.role_selected)
        studentTab.setTextColor(resources.getColor(android.R.color.white))
        adminTab.background = null
        adminTab.setTextColor(resources.getColor(android.R.color.darker_gray))
    }

    private fun selectAdminTab() {
        selectedRole = "admin"
        adminTab.setBackgroundResource(R.drawable.role_selected)
        adminTab.setTextColor(resources.getColor(android.R.color.white))
        studentTab.background = null
        studentTab.setTextColor(resources.getColor(android.R.color.darker_gray))
    }

    // ---------------- LOGIN ----------------

    private fun loginUser() {

        val mail = email.text.toString().trim()
        val pass = password.text.toString().trim()

        if (TextUtils.isEmpty(mail) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Email and Password required", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(mail, pass)
            .addOnSuccessListener {
                redirectBasedOnRole(auth.currentUser!!.uid)
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }

    // ---------------- ROLE CHECK & REDIRECT ----------------

    private fun redirectBasedOnRole(uid: String) {

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    auth.signOut()
                    return@addOnSuccessListener
                }

                val roleInDb = doc.getString("role") ?: "student"

                // ❌ Admin trying to login via Student tab
                if (roleInDb == "admin" && selectedRole == "student") {
                    Toast.makeText(
                        this,
                        "You are an admin. Please login using Admin tab.",
                        Toast.LENGTH_LONG
                    ).show()
                    auth.signOut()
                    return@addOnSuccessListener
                }

                val intent = if (roleInDb == "admin") {
                    Intent(this, AdminDashboardActivity::class.java)
                } else {
                    Intent(this, StudentDashboardActivity::class.java)
                }

                // 🔥 CLEAR BACK STACK (IMPORTANT)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to verify role", Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
    }
}
