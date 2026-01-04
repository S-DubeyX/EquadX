package com.team.equadx

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.app.AppCompatDelegate


class SignupActivity : AppCompatActivity() {

    private lateinit var fullName: EditText
    private lateinit var studentId: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var branchSpinner: Spinner
    private lateinit var signupBtn: Button
    private lateinit var loginText: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
        )

        setContentView(R.layout.activity_signup) // or signup
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // -------- Bind Views --------
        fullName = findViewById(R.id.fullName)
        studentId = findViewById(R.id.studentId)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirmPassword)
        branchSpinner = findViewById(R.id.branchSpinner)
        signupBtn = findViewById(R.id.signupBtn)
        loginText = findViewById(R.id.loginText)

        // -------- Branch Spinner --------
        ArrayAdapter.createFromResource(
            this,
            R.array.branch_list,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            branchSpinner.adapter = adapter
        }

        signupBtn.setOnClickListener {
            registerStudent()
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerStudent() {

        val name = fullName.text.toString().trim()
        val sid = studentId.text.toString().trim()
        val mail = email.text.toString().trim()
        val pass = password.text.toString().trim()
        val confirm = confirmPassword.text.toString().trim()
        val branch = branchSpinner.selectedItem.toString()

        // -------- Validation --------
        if (TextUtils.isEmpty(name) ||
            TextUtils.isEmpty(sid) ||
            TextUtils.isEmpty(mail) ||
            TextUtils.isEmpty(pass)
        ) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (branch == "Select Branch") {
            Toast.makeText(this, "Please select a branch", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        signupBtn.isEnabled = false

        // -------- Firebase Auth --------
        auth.createUserWithEmailAndPassword(mail, pass)
            .addOnSuccessListener {

                val uid = auth.currentUser!!.uid

                // 🔥 USER DATA (UID AS DOCUMENT ID)
                val studentData = hashMapOf(
                    "fullName" to name,
                    "studentId" to sid,
                    "email" to mail,
                    "branch" to branch,
                    "role" to "student",
                    "points" to 0L   // ✅ MUST be Long
                )

                firestore.collection("users")
                    .document(uid)
                    .set(studentData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "Account created successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Go to Login (clean state)
                        startActivity(
                            Intent(this, LoginActivity::class.java)
                        )
                        finish()
                    }
                    .addOnFailureListener {
                        signupBtn.isEnabled = true
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener {
                signupBtn.isEnabled = true
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }
}
