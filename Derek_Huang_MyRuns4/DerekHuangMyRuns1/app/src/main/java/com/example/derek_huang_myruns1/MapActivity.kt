package com.example.derek_huang_myruns1

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var saveButton : Button
    private lateinit var cancelButton : Button
    private lateinit var mMap: GoogleMap
    private lateinit var locationManager: LocationManager
    private val PERMISSION_REQUEST_CODE = 0
    private val pathPoints = mutableListOf<LatLng>()
    private var startMarker: Marker? = null
    private var currentLocationMarker: Marker? = null

    private lateinit var typeTextView: TextView
    private lateinit var avgSpeedTextView: TextView
    private lateinit var curSpeedTextView: TextView
    private lateinit var climbTextView: TextView
    private lateinit var calorieTextView: TextView
    private lateinit var distanceTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        checkPermission()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView)
                as SupportMapFragment
        Log.d("MapActivity", "Attempting to get map async")
        mapFragment.getMapAsync(this)

        // Start TrackingService
        startTrackingService()

        //Buttons and TextViews
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        typeTextView = findViewById(R.id.mapType)
        avgSpeedTextView = findViewById(R.id.mapAvgSpeed)
        curSpeedTextView = findViewById(R.id.mapCurSpeed)
        climbTextView = findViewById(R.id.mapClimb)
        calorieTextView = findViewById(R.id.mapCalorie)
        distanceTextView = findViewById(R.id.mapDistance)

        //Button Functionality (just close activity for now)
        cancelButton.setOnClickListener {
            finish()
        }
        //TODO: save entries for mylab3
        saveButton.setOnClickListener {
            finish()
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        observeLocationUpdates()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        else
            observeLocationUpdates()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                startTrackingService()
                val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
                mapFragment.getMapAsync(this)
            } else {
                // Permission was denied
                finish()
            }
        }
    }

    private fun startTrackingService() {
        // Check for foreground service permission as well if targeting Android 9 (Pie) or above
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) {

            val serviceIntent = Intent(this, TrackingService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }
    private fun stopTrackingService() {
        val serviceIntent = Intent(this, TrackingService::class.java)
        stopService(serviceIntent)
    }

    //inspired by demo code
    private fun observeLocationUpdates() {
        TrackingService.locationUpdates.observe(this, Observer { location ->
            // Update the map with the new location
            onLocationChanged(location)
            Log.d("LOCATION TRACKER", "LOCATION CHANGED")
        })
    }
    //inspired by demo code
    private fun onLocationChanged(location: Location) {
        if (!::mMap.isInitialized) { // Correct way to check for lateinit initialization
            Log.d("MapActivity", "mMap not initialized yet.")
            return
        }
        val userLocation = LatLng(location.latitude, location.longitude)
        Log.d("LOCATION TRACKER", "$userLocation")
        //if its the first location update, set the start marker
        if (startMarker == null) {
            startMarker = mMap.addMarker(
                MarkerOptions()
                    .position(userLocation)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
        }

        //update current location marker or create it if it doesn't exist
        if (currentLocationMarker == null) {
            currentLocationMarker = mMap.addMarker(
                MarkerOptions()
                    .position(userLocation)
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        } else {
            currentLocationMarker!!.position = userLocation
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

        //add new point to the path and update the polyline
        pathPoints.add(userLocation)
        drawPolyline()
    }

    private fun drawPolyline() {
        val polylineOptions = PolylineOptions().addAll(pathPoints).width(5f).color(Color.BLACK)
        mMap.addPolyline(polylineOptions)
    }

    override fun onDestroy() {
        stopTrackingService()
        super.onDestroy()
    }
}