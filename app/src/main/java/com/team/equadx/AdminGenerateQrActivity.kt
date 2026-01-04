package com.team.equadx

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class AdminGenerateQrActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_generate_qr)
        supportActionBar?.hide()

        val etQrText = findViewById<EditText>(R.id.etQrText)
        val btnGenerate = findViewById<Button>(R.id.btnGenerate)
        val imgQr = findViewById<ImageView>(R.id.imgQr)

        btnGenerate.setOnClickListener {

            val binId = etQrText.text.toString().trim()

            if (binId.isEmpty()) {
                Toast.makeText(this, "Enter Bin ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ STANDARD QR FORMAT
            val qrText = "EQUADX_BIN:$binId"

            // ✅ ONLY UPDATE SAFE FIELDS (NO LOCATION LOSS)
            val binData = hashMapOf(
                "active" to true,
                "points" to 10,
                "qrText" to qrText,
                "updatedAt" to System.currentTimeMillis()
            )

            // 🔥 USE MERGE (CRITICAL FIX)
            db.collection("bins")
                .document(binId)
                .set(binData, SetOptions.merge())
                .addOnSuccessListener {

                    // ✅ Generate QR only after Firestore success
                    imgQr.setImageBitmap(generateQr(qrText))

                    Toast.makeText(
                        this,
                        "QR generated safely (location preserved)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this,
                        "Failed to generate QR",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun generateQr(text: String): Bitmap {

        val size = 500
        val bitMatrix: BitMatrix =
            MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        return bitmap
    }
}
