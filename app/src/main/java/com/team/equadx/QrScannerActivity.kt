package com.team.equadx

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class QrScannerActivity : AppCompatActivity() {

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1001
    }

    private lateinit var previewView: PreviewView
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var scanned = false

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        previewView = findViewById(R.id.previewView)

        val scanLine = findViewById<View>(R.id.scanLine)
        scanLine.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.scan_line_anim)
        )

        // 🔐 CAMERA PERMISSION CHECK
        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    // ---------------- PERMISSION ----------------

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to scan QR codes",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    // ---------------- CAMERA ----------------

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                if (!scanned) {
                    processImage(imageProxy)
                } else {
                    imageProxy.close()
                }
            }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    // ---------------- QR PROCESSING ----------------

    private fun processImage(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        BarcodeScanning.getClient()
            .process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty() && !scanned) {
                    scanned = true
                    val rawValue = barcodes[0].rawValue ?: ""
                    handleQr(rawValue)
                }
            }
            .addOnFailureListener {
                scanned = false
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    // ---------------- QR VALIDATION ----------------

    private fun handleQr(rawValue: String) {

        // ✅ STRICT PREFIX CHECK
        if (!rawValue.startsWith("EQUADX_BIN:")) {
            scanned = false
            showResult("Invalid QR ❌")
            return
        }

        val binId = rawValue.removePrefix("EQUADX_BIN:")
        validateAndReward(binId)
    }

    // ---------------- FIRESTORE LOGIC ----------------

    private fun validateAndReward(binId: String) {

        val uid = auth.currentUser?.uid
        if (uid == null) {
            scanned = false
            showResult("Session expired. Please login again.")
            return
        }

        val fusedLocationClient =
            com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { userLocation ->

                if (userLocation == null) {
                    scanned = false
                    showResult("Unable to get your location")
                    return@addOnSuccessListener
                }

                val userLat = userLocation.latitude
                val userLng = userLocation.longitude

                val userRef = db.collection("users").document(uid)
                val scanRef = userRef.collection("scans").document(binId)
                val binRef = db.collection("bins").document(binId)

                val today =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                scanRef.get().addOnSuccessListener { scanDoc ->

                    if (scanDoc.getString("lastScanDate") == today) {
                        scanned = false
                        showResult("Already scanned today ⚠️")
                        return@addOnSuccessListener
                    }

                    binRef.get().addOnSuccessListener { binDoc ->

                        if (!binDoc.exists() || binDoc.getBoolean("active") != true) {
                            scanned = false
                            showResult("Invalid QR ❌")
                            return@addOnSuccessListener
                        }

                        val binLat = binDoc.getDouble("lat")
                        val binLng = binDoc.getDouble("lng")

                        if (binLat == null || binLng == null) {
                            scanned = false
                            showResult("Bin location missing")
                            return@addOnSuccessListener
                        }

                        // 📏 DISTANCE CHECK
                        val results = FloatArray(1)
                        android.location.Location.distanceBetween(
                            userLat,
                            userLng,
                            binLat,
                            binLng,
                            results
                        )

                        val distanceInMeters = results[0]

                        if (distanceInMeters > 30) {
                            scanned = false
                            showResult("You are too far from the bin (${distanceInMeters.toInt()}m)")
                            return@addOnSuccessListener
                        }

                        // ✅ USER IS NEAR → GIVE POINTS
                        val reward = binDoc.getLong("points") ?: 10

                        db.runTransaction { tx ->
                            val userSnap = tx.get(userRef)
                            val current = userSnap.getLong("points") ?: 0

                            tx.update(userRef, "points", current + reward)
                            tx.set(
                                scanRef,
                                mapOf(
                                    "lastScanDate" to today,
                                    "points" to reward,
                                    "distance" to distanceInMeters,
                                    "timestamp" to System.currentTimeMillis()
                                )
                            )
                        }.addOnSuccessListener {

                            val intent = Intent()
                            intent.putExtra("QR_SUCCESS", true)
                            intent.putExtra("QR_POINTS", reward)
                            setResult(Activity.RESULT_OK, intent)
                            finish()
                        }
                    }
                }
            }
            .addOnFailureListener {
                scanned = false
                showResult("Location permission required")
            }
    }


    // ---------------- RESULT ----------------

    private fun showResult(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
