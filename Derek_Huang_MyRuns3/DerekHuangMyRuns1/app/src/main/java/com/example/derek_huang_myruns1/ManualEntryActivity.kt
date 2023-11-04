package com.example.derek_huang_myruns1

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.derek_huang_myruns1.database.ExerciseEntry
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabase
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabaseDao
import com.example.derek_huang_myruns1.database.ExerciseRepository
import com.example.derek_huang_myruns1.database.ExerciseViewModel
import com.example.derek_huang_myruns1.database.ExerciseViewModelFactory
import java.util.Calendar

class ManualEntryActivity : AppCompatActivity() {
    private val info = arrayOf(
        "Date", "Time", "Duration", "Distance", "Calories", "Heart Rate", "Comment"
    )
    private lateinit var myListView: ListView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private val calendar = java.util.Calendar.getInstance()
    private lateinit var viewModel: ExerciseViewModel
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var database: ExerciseEntryDatabase

    private var duration: Double? = null
    private var distance: Double? = null
    private var calories: Int? = null
    private var heartRate: Int? = null
    private var comment: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        myListView = findViewById(R.id.myListView)

        // Obtain the ViewModel - make sure to use the correct constructor for your ViewModelFactor
        database = ExerciseEntryDatabase.getInstance(this)
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)

        //taken from lecture
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1, info
        )
        myListView.adapter = arrayAdapter

        //Button Functionality (just close activity for now)
        cancelButton.setOnClickListener {
            finish()
        }
        //TODO: save entries for mylab3
        saveButton.setOnClickListener {
            val newEntry = ExerciseEntry(
                id = 0L, // ID is auto-generated
                inputType = 1,
                activityType = 1, //activity that they chose previously
                dateTime = Calendar.getInstance(),
                duration = duration ?: 0.0,
                distance = distance ?: 0.0,
                heartRate = heartRate?.toDouble() ?: 0.0,
                comment = comment ?: ""
            )
            try {
                viewModel.insertEntry(newEntry)
                Log.d("SAVED ENTRIES", "Entry inserted into database")
                val allEntries = databaseDao.getAllEntries()
                allEntries.observe(this, Observer { entries ->
                    if (entries != null) {
                        for (entry in entries) {
                            Log.d("DatabaseCheck", "Entry: $entry")
                        }
                    }
                })


            } catch (e: Exception) {
                Log.e("SAVED ENTRIES", "Error inserting entry into database: ${e.message}")
            }
            finish()
        }

        myListView.setOnItemClickListener{_, _, position, _, ->
            when (position) {
                0 -> showDatePickerDialog()
                1 -> showTimePickerDialog()
                2 -> showEditTextDialogInt("Duration")
                3 -> showEditTextDialogInt("Distance")
                4 -> showEditTextDialogInt("Calories")
                5 -> showEditTextDialogInt("Heart Rate")
                6 -> showEditTextDialogString("Comment")
            }
        }
    }

    //Inspired from layout kotlin
    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance() // Create a Calendar instance to set the initial date

        // Create a DatePickerDialog using an anonymous function as the onClickListener
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Handle the selected date here
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, monthOfYear)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }
    //inspired from layoutkotlin
    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance() // Create a Calendar instance to set the initial time

        // Create a TimePickerDialog using an anonymous function as the onTimeSetListener
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // Handle the selected time here
                val selectedTime = Calendar.getInstance()
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    private fun showEditTextDialogInt(title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("$title")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)
        builder.setPositiveButton("OK") { _, _ ->
            val enteredText = input.text.toString()
            when (title) {
                "Duration" -> duration = enteredText.toDoubleOrNull()
                "Distance" -> distance = enteredText.toDoubleOrNull()
                "Calories" -> calories = enteredText.toIntOrNull()
                "Heart Rate" -> heartRate = enteredText.toIntOrNull()
            }
            Log.d("DIALOG", "ENTERED DATA FOR $title: $enteredText")

        }

        builder.setNegativeButton("CANCEL") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun showEditTextDialogString(title: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("$title")

        val input = EditText(this)
        input.hint = ("How did it go? Notes here.")
        builder.setView(input)
        builder.setPositiveButton("OK") { _, _ ->
            val enteredText = input.text.toString()
            if (title == "Comment") {
                comment = enteredText
            }
        }

        builder.setNegativeButton("CANCEL") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}