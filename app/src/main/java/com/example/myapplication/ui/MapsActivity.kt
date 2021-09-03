package com.example.myapplication.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityMapsBinding
import com.example.myapplication.model.LocationLog
import com.example.myapplication.model.LocationLogging
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    //Late-initializing variables
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val LOCATION_PERMISSION_REQUEST = 1
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    lateinit var databaseRef: DatabaseReference

    //Function requesting location permission
    private fun getLocationAccess(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            map.isMyLocationEnabled = true
            getLocationUpdates()
            startLocationUpdate()
        }else{
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST){
            try {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)){
                    map.isMyLocationEnabled = true
                }else{
                    Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show()
                }
            }catch (e: SecurityException){
                Log.d("Map", e.message!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        databaseRef = Firebase.database.reference

        databaseRef.addValueEventListener(logListener)


    }


    //variable to get partner's location from firebase
    val logListener = object : ValueEventListener {
        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(this@MapsActivity, "Could not read from database", Toast.LENGTH_SHORT).show()
        }
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()){
                val locationLogging = snapshot.child("TrackSamuel").getValue(LocationLog::class.java)
                val userLat = locationLogging?.latitude
                val userLog = locationLogging?.longitude
                if (userLat != null && userLog != null){
                    val userLocation = LatLng(userLat, userLog)
                    map.clear()
                    val markerOptions = MarkerOptions().position(userLocation).title("Samuel Location")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_samuel))
                    map.addMarker(markerOptions)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 20f))
                }
            }
        }

    }

    //getLocationUpdate function to set longitude and latitude and set to firebase
    fun getLocationUpdates(){
        locationRequest = LocationRequest()
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 20000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()){
                    val location = locationResult.lastLocation

                    databaseRef = Firebase.database.reference
                    val locationLogging = LocationLogging(location.latitude, location.longitude)
                    databaseRef.child("userLocation").setValue(locationLogging)
                        .addOnSuccessListener {
                            Toast.makeText(this@MapsActivity, "location written into the database", Toast.LENGTH_SHORT).show()
                        }
                    if (location != null){
                        val latLng = LatLng(location.latitude, location.longitude)
                        map.clear()
                        val markerOption = MarkerOptions().position(latLng).title("My Location")
                        map.addMarker(markerOption)
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                }
            }
        }
    }


    //function to get location updates
    @SuppressLint("MissingPermission")
    fun startLocationUpdate(){
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        getLocationAccess()

    }
}