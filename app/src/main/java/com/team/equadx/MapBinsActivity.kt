package com.team.equadx

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapBinsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_bins)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        loadBins()

        // 📍 Open Google Maps navigation on marker click
        mMap.setOnInfoWindowClickListener { marker ->
            val lat = marker.position.latitude
            val lng = marker.position.longitude

            val uri = Uri.parse("google.navigation:q=$lat,$lng")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")

            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadBins() {

        db.collection("bins")
            .whereEqualTo("active", true)
            .get()
            .addOnSuccessListener { snapshot ->

                var cameraMoved = false

                for (doc in snapshot.documents) {

                    val lat = doc.getDouble("lat")
                    val lng = doc.getDouble("lng")
                    val name = doc.getString("name") ?: "QR Bin"

                    // ❗ SAFETY CHECK (NO CRASH)
                    if (lat == null || lng == null) continue

                    val position = LatLng(lat, lng)

                    mMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(name)
                            .snippet("Tap to navigate")
                    )

                    // 🎯 Move camera only once (first valid bin)
                    if (!cameraMoved) {
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(position, 16f)
                        )
                        cameraMoved = true
                    }
                }

                if (!cameraMoved) {
                    Toast.makeText(this, "No active bins found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load bins", Toast.LENGTH_SHORT).show()
            }
    }
}
