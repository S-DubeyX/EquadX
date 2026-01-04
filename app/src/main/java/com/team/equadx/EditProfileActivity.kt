package com.team.equadx

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        supportActionBar?.hide()

        val nameInput = findViewById<EditText>(R.id.nameInput)
        val branchInput = findViewById<EditText>(R.id.branchInput)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)

        // ✅ Load existing data
        userRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nameInput.setText(doc.getString("fullName") ?: "")
                    branchInput.setText(doc.getString("branch") ?: "")
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }

        // ✅ Save updated data
        saveBtn.setOnClickListener {

            val newName = nameInput.text.toString().trim()
            val newBranch = branchInput.text.toString().trim()

            if (newName.isEmpty() || newBranch.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updateData = mapOf(
                "fullName" to newName,
                "branch" to newBranch
            )

            userRef.set(updateData, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
