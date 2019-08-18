package com.zainco.uber_simcoder

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_driver_map.*


class DriverMapActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    @SuppressLint("MissingPermission")
    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 2000//update every second
        locationRequest.fastestInterval = 1000//update every second
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        //display location for the first time on the map
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                displayLocation(location)
            }
        //update location every second here
        //2 choices for requestlocation u
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                val location = locationResult!!.lastLocation
                displayLocation(location)
                val userId = FirebaseAuth.getInstance().currentUser!!.uid
                val firebaseReference = FirebaseDatabase.getInstance().reference.child("driversavailable")
                val geoFire = GeoFire(firebaseReference)
                geoFire.setLocation(
                    userId,
                    GeoLocation(location.latitude, location.longitude)
                ) { key, error ->
                    Toast.makeText(
                        this@DriverMapActivity,
                        (key == userId).toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }, mainLooper)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        buildLocationRequest()
        mMap.isMyLocationEnabled = true
    }

    fun displayLocation(location: Location?) {
        location?.let {
            lastLocation = location
            val latLng = LatLng(location.latitude, location.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap.animateCamera(CameraUpdateFactory.zoomTo(20f))
        }
    }

    override fun onStop() {
        super.onStop()
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val firebaseReference = FirebaseDatabase.getInstance().getReference("driversavailable")
        val geoFire = GeoFire(firebaseReference)
        geoFire.removeLocation(userId) { key, error ->
            Toast.makeText(
                this@DriverMapActivity,
                "removed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
