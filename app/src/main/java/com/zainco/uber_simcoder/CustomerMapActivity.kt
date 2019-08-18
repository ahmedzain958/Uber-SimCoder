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
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_customer_map.*

class CustomerMapActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var pickupLocation: LatLng
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        request.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            val ref = FirebaseDatabase.getInstance().reference.child("customerRequest")
            // to save current customer location(From location)
            val geoFire = GeoFire(ref)
            geoFire.setLocation(
                userId, GeoLocation(
                    lastLocation.latitude,
                    lastLocation.longitude
                )
            ) { _, _ ->
                //doesnt work without this listener
            }
            // to inform the customer from where (customer's first location) he requested the driver
            pickupLocation = LatLng(
                lastLocation.latitude,
                lastLocation.longitude
            )
            mMap.addMarker(MarkerOptions().position(pickupLocation).title("pick me up here"))
            request.text = "Getting ur driver"

            getClosestDriver()
        }
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    var driverFound = false
    var radius = 1.0
    var driverCount = 0
    lateinit var driverFoundId: String

    private fun getClosestDriver() {
        val driversLocations = FirebaseDatabase.getInstance().reference.child("driversavailable")
        val geoFire = GeoFire(driversLocations)
        val geoQuery = geoFire.queryAtLocation(
            GeoLocation(
                pickupLocation.latitude,
                pickupLocation.longitude
            ), radius
        )
        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onGeoQueryReady() {// finished searching for drivers
                if (!driverFound) {
                    radius = radius.inc()
                    getClosestDriver()
                }
            }

            override fun onKeyEntered(key: String?, location: GeoLocation?) {
                if (!driverFound) {//called for every driver
                    driverFound = true
                    driverFoundId = key!!
                    driverCount = driverCount.inc()
                    mMap.addMarker(MarkerOptions().position(
                        LatLng(location!!.latitude, location.longitude)
                    ).title("driver no $driverCount"))
                }
            }

            override fun onKeyMoved(key: String?, location: GeoLocation?) {
                Toast.makeText(
                    this@CustomerMapActivity,
                    " sign up error",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onKeyExited(key: String?) {
                Toast.makeText(
                    this@CustomerMapActivity,
                    " sign up error",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onGeoQueryError(error: DatabaseError?) {
                Toast.makeText(
                    this@CustomerMapActivity,
                    " sign up error",
                    Toast.LENGTH_SHORT
                ).show()
            }


        })
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
            mMap.animateCamera(CameraUpdateFactory.zoomTo(10f))
        }
    }

    override fun onStop() {
        super.onStop()

    }
}
