package com.example.derek_huang_myruns1

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabase
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabaseDao
import com.example.derek_huang_myruns1.database.ExerciseRepository
import com.example.derek_huang_myruns1.database.ExerciseViewModel
import com.example.derek_huang_myruns1.database.ExerciseViewModelFactory

class EntryActivity : AppCompatActivity() {

    private lateinit var viewModel: ExerciseViewModel
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var database: ExerciseEntryDatabase
    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        deleteButton = findViewById(R.id.delete_button)
        cancelButton = findViewById(R.id.cancel_button)

        // Obtain the ViewModel
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)

        val entryId = intent.getLongExtra("ENTRY_ID", -1)

        viewModel.getEntryById(entryId).observe(this, Observer { entry ->
            // Make sure the entry is not null
            entry?.let {
                findViewById<TextView>(R.id.tv_entry_type).text = it.getEntryTypeString()
                findViewById<TextView>(R.id.tv_activity_type).text = it.getActivityTypeString()
                findViewById<TextView>(R.id.tv_date_time).text = it.getFormattedDateTime()
                findViewById<TextView>(R.id.tv_duration).text = it.getFormattedDuration()
                findViewById<TextView>(R.id.tv_distance).text = formatDistance(it.distance)
                findViewById<TextView>(R.id.tv_calories).text = "${it.calories.toInt()} cals"
                findViewById<TextView>(R.id.tv_heart_rate).text = "${it.heartRate.toInt()} bpm"
            }
        })
        //TODO: DELETE FUNCTIONALITY
        deleteButton.setOnClickListener {
            viewModel.deleteEntry(entryId)
            Toast.makeText(this, "Deleted entry: ${entryId}", Toast.LENGTH_SHORT).show()
            finish()
        }

        cancelButton.setOnClickListener {
            finish()
        }

    }


    private fun formatDistance(distanceMiles: Double): String {
        val unitPreference = sharedPreferences.getString("unit_preference", "Miles") ?: "Miles"
        val distance: Double
        val unit: String

        if (unitPreference == "Imperial") {
            distance = distanceMiles
            unit = "Miles"
        } else { // Assuming "Kilometers" as the only other option
            distance = distanceMiles * 1.60934
            unit = "Kilometers"
        }

        return String.format("%.2f %s", distance, unit)
    }
}