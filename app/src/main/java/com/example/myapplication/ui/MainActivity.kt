package com.example.myapplication.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.internal.IGoogleMapDelegate


class MainActivity : AppCompatActivity() {
    private lateinit var maps: GoogleMap
    private val LOCATION_PERMISSION_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val trackBtn = findViewById<Button>(R.id.track)
        trackBtn.setOnClickListener {
            getLocationAccess()
        }
    }

    fun intent(){
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun getLocationAccess(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            intent()
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
                    intent()
                }else{
                    Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show()

                }
            }catch (e: SecurityException){
                Log.d("Map", e.message!!)
            }
        }else{
            Toast.makeText(this, "Permission denied to access location", Toast.LENGTH_SHORT).show()
        }
    }

}
