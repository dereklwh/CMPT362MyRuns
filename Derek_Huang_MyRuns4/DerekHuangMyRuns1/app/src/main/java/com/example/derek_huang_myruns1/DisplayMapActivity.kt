package com.example.derek_huang_myruns1

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.derek_huang_myruns1.database.ExerciseEntry
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabase
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabaseDao
import com.example.derek_huang_myruns1.database.ExerciseEntryListAdapter
import com.example.derek_huang_myruns1.database.ExerciseRepository
import com.example.derek_huang_myruns1.database.ExerciseViewModel
import com.example.derek_huang_myruns1.database.ExerciseViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class DisplayMapActivity : AppCompatActivity() {

    private lateinit var exerciseEntryAdapter: ExerciseEntryListAdapter
    private lateinit var viewModel: ExerciseViewModel
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var database: ExerciseEntryDatabase
    private lateinit var mMap: GoogleMap
    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_map)

        deleteButton = findViewById(R.id.delete_button)
        cancelButton = findViewById(R.id.cancel_button)

        // Obtain the ViewModel
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)

        val entryId = intent.getLongExtra("ENTRY_ID", -1)

        viewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
            displayRoute(entryId)
        }

        deleteButton.setOnClickListener {
            viewModel.deleteEntry(entryId)
            Toast.makeText(this, "Deleted entry: ${entryId}", Toast.LENGTH_SHORT).show()
            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun displayRoute(entryId: Long) {
        viewModel.getEntryById(entryId).observe(this, { entry ->
            if (entry != null && !isFinishing) {
                drawRoute(entry.locationList)
                updateUI(entry)
            }
        })
    }

    private fun drawRoute(locationList: ArrayList<LatLng>) {
        if (locationList.isNotEmpty()) {
            //create PolylineOptions to draw the route
            val polylineOptions = PolylineOptions()
                .addAll(locationList)
                .width(5f)
                .color(Color.BLACK)

            //add the polyline to the map
            mMap.addPolyline(polylineOptions)

            val startPoint = locationList.first()
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15f))

            val startMarkerOptions = MarkerOptions()
                .position(startPoint)
                .title("Start")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) // Green marker for start
            mMap.addMarker(startMarkerOptions)

            val endPoint = locationList.last()
            val endMarkerOptions = MarkerOptions()
                .position(endPoint)
                .title("End")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Red marker for end
            mMap.addMarker(endMarkerOptions)
        }
    }
    private fun updateUI(entry: ExerciseEntry) {
        findViewById<TextView>(R.id.mapType).text = String.format(getString(R.string.type_format), entry.getActivityTypeString())
        findViewById<TextView>(R.id.mapAvgSpeed).text = String.format(getString(R.string.avg_speed_format), entry.avgSpeed)
        findViewById<TextView>(R.id.mapCurSpeed).text = String.format("Cur speed: N/A")
        findViewById<TextView>(R.id.mapClimb).text = String.format(getString(R.string.climb_format), entry.climb)
        findViewById<TextView>(R.id.mapCalorie).text = String.format(getString(R.string.calorie_format), entry.calories.toInt())
        findViewById<TextView>(R.id.mapDistance).text = String.format(getString(R.string.distance_format), entry.distance)
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}