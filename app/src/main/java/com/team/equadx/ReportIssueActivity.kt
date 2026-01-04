package com.team.equadx

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class ReportIssueActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_issue)

        supportActionBar?.hide()

        val subjectSpinner = findViewById<Spinner>(R.id.subjectSpinner)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val submitBtn = findViewById<Button>(R.id.submitBtn)

        // Spinner data
        val subjects = listOf("Bin Full", "Damaged Bin", "Overflowing", "Other")
        subjectSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            subjects
        )

        submitBtn.setOnClickListener {
            val subject = subjectSpinner.selectedItem.toString()
            val message = messageInput.text.toString().trim()

            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitReport(subject, message)
        }
    }

    private fun submitReport(subject: String, message: String) {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val reportData = hashMapOf(
            "userId" to uid,
            "subject" to subject,
            "message" to message,
            "timestamp" to Date()
        )

        db.collection("reports")
            .add(reportData)
            .addOnSuccessListener {
                Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to submit report", Toast.LENGTH_LONG).show()
            }
    }
}
