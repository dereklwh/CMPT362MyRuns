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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.derek_huang_myruns1.database.ExerciseEntry
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabase
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabaseDao
import com.example.derek_huang_myruns1.database.ExerciseRepository
import com.example.derek_huang_myruns1.database.ExerciseViewModel
import com.example.derek_huang_myruns1.database.ExerciseViewModelFactory
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
import java.util.Calendar

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

    private var totalDistance = 0f // Total distance in meters
    private var startTimeInMillis = 0L // Start time of the activity
    private var lastLocation: Location? = null // Last known location
    private var timeElapsedInSeconds = 0L
    private var caloriesBurned = 0f
    private val selectedDateTime = Calendar.getInstance()
    private var avgSpeed = 0f
    private var comment: String? = null
    private var avgPace: Double? = null
    private var climb: Double? = null
    private var heartRate: Double? = null

    private lateinit var viewModel: ExerciseViewModel
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var database: ExerciseEntryDatabase



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // Obtain the ViewModel
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)

        checkPermission()
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView)
                as SupportMapFragment
        Log.d("MapActivity", "Attempting to get map async")
        mapFragment.getMapAsync(this)

        //Buttons and TextViews
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        typeTextView = findViewById(R.id.mapType)
        avgSpeedTextView = findViewById(R.id.mapAvgSpeed)
        curSpeedTextView = findViewById(R.id.mapCurSpeed)
        climbTextView = findViewById(R.id.mapClimb)
        calorieTextView = findViewById(R.id.mapCalorie)
        distanceTextView = findViewById(R.id.mapDistance)


        val activityTypesArray = resources.getStringArray(R.array.ui_activity_type_spinner)
        val selectedActivityTypeId = intent.getIntExtra("SELECTED_ACTIVITY_TYPE_ID", -1)
        val selectedInputTypeId = intent.getIntExtra("SELECTED_INPUT_TYPE_ID", -1)
        //typeTextView.text = String.format(getString(R.string.type_format), activityTypesArray[selectedActivityTypeId])

        // Start TrackingService
        startTrackingService()

        if (selectedInputTypeId == 2) { // Automatic
            TrackingService.activityTypeUpdates.observe(this, Observer { activityType ->
                typeTextView.text = String.format(getString(R.string.type_format), activityType)
            })
        } else if (selectedInputTypeId == 1) { // GPS
            typeTextView.text = String.format(getString(R.string.type_format), activityTypesArray[selectedActivityTypeId])
        }

        //Button Functionality
        cancelButton.setOnClickListener {
            finish()
        }
        //TODO: save entries for mylab3
        saveButton.setOnClickListener {
            val locationArrayList = ArrayList(pathPoints)

            // Determine activity type based on input type
            val activityType = if (selectedInputTypeId == 2) { // Automatic
                // Convert the activity type string to its corresponding integer ID
                convertActivityTypeToInt(TrackingService.activityTypeUpdates.value)
            } else { // GPS
                selectedActivityTypeId
            }

            Log.d("LIST ENTRIES", locationArrayList.joinToString())
            val newEntry = ExerciseEntry(
                inputType = selectedInputTypeId,
                activityType = activityType,
                dateTime = selectedDateTime,
                duration = timeElapsedInSeconds.toDouble() ?: 0.0,
                distance = totalDistance.toDouble() ?: 0.0,
                calories = caloriesBurned.toDouble() ?: 0.0,
                heartRate = heartRate ?: 0.0,
                comment = comment ?: "",
                locationList = locationArrayList,
                avgPace = avgPace ?: 0.0,
                avgSpeed = avgSpeed.toDouble() ?: 0.0,
                climb = climb ?: 0.0
            )
            try {
                viewModel.insertEntry(newEntry)
                Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show()
                Log.d("SAVED ENTRIES", "Entry inserted into database")
            } catch (e: Exception) {
                Log.e("SAVED ENTRIES", "Error inserting entry into database: ${e.message}")
            }
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
        if (!::mMap.isInitialized) { //check for lateinit initialization
            Log.d("MapActivity", "mMap not initialized yet.")
            return
        }
        val userLocation = LatLng(location.latitude, location.longitude)

        //distance in km
        lastLocation?.let { last ->
            this.totalDistance += last.distanceTo(location)/1000
        }
        lastLocation = location

        //current speed (1m/s = 3.6 km/h)
        val currentSpeed = location.speed * 3.6f

        //average speed
        if (startTimeInMillis == 0L){
            startTimeInMillis = System.currentTimeMillis()
        }

        this.timeElapsedInSeconds = (System.currentTimeMillis() - startTimeInMillis) / 1000
        Log.d("SECONDS ELAPSED", "TIME ELAPSED: $timeElapsedInSeconds")
        avgSpeed = if (timeElapsedInSeconds > 0) totalDistance / (timeElapsedInSeconds/3600f) else 0f

        //calories (use generic formula: 80Kcal/km)
        this.caloriesBurned = totalDistance * 80

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
        updateUI(avgSpeed, currentSpeed, caloriesBurned, totalDistance)
    }

//    private fun calculateDistanceIncrement(lastLocation: Location?, newLocation: Location): Float {
//        return lastLocation?.distanceTo(newLocation) ?: 0f
//    }

    private fun drawPolyline() {
        val polylineOptions = PolylineOptions().addAll(pathPoints).width(5f).color(Color.BLACK)
        mMap.addPolyline(polylineOptions)
    }

    private fun updateUI(avgSpeed: Float, curSpeed: Float, calories: Float, distance: Float) {
        val unitPreference = getUnitPreference()
        val formattedAvgSpeed = formatSpeed(avgSpeed, unitPreference)
        val formattedCurSpeed = formatSpeed(curSpeed, unitPreference)
        val formattedDistance = formatDistance(distance, unitPreference)

        avgSpeedTextView.text = formattedAvgSpeed
        curSpeedTextView.text = formattedCurSpeed
        calorieTextView.text = String.format(getString(R.string.calorie_format), calories.toInt())
        distanceTextView.text = formattedDistance
    }


    private fun getUnitPreference(): String {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        return sharedPreferences.getString("unit_preference", "Miles") ?: "Miles"
    }

    private fun formatSpeed(speed: Float, unitPreference: String): String {
        val speedInPreferredUnit = if (unitPreference == "Imperial") {
            //convert speed to miles per hour
            speed / 1.60934f
        } else {
            //speed is already in km/h
            speed
        }
        return String.format("%.2f %s", speedInPreferredUnit, if (unitPreference == "Imperial") "mph" else "km/h")
    }

    private fun formatDistance(distance: Float, unitPreference: String): String {
        val distanceInPreferredUnit = if (unitPreference == "Imperial") {
            //convert distance to miles
            distance / 1.60934f
        } else {
            //distance is already in kilometers
            distance
        }
        return String.format("%.2f %s", distanceInPreferredUnit, if (unitPreference == "Imperial") "Miles" else "Kilometers")
    }

    override fun onDestroy() {
        stopTrackingService()
        super.onDestroy()
    }

    // Helper function to convert activity type string to integer ID
    private fun convertActivityTypeToInt(activityType: String?): Int {
        return when (activityType) {
            "Running" -> 0
            "Walking" -> 1
            "Standing" -> 2
            else -> -1
        }
    }
}