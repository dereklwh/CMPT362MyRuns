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
import android.widget.Toast
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

    private val selectedDateTime = Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        myListView = findViewById(R.id.myListView)

        // Obtain the ViewModel
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

        saveButton.setOnClickListener {
            val selectedActivityTypeId = intent.getIntExtra("SELECTED_ACTIVITY_TYPE_ID", -1)
            //val selectedDateTime = Calendar.getInstance()
            val newEntry = ExerciseEntry(
                id = 0L, // ID is auto-generated
                inputType = 1,
                activityType = selectedActivityTypeId,
                dateTime = selectedDateTime,
                duration = duration ?: 0.0,
                distance = distance ?: 0.0,
                calories = calories?.toDouble() ?: 0.0,
                heartRate = heartRate?.toDouble() ?: 0.0,
                comment = comment ?: ""
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
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                // Update the class-level Calendar object
                selectedDateTime.set(Calendar.YEAR, year)
                selectedDateTime.set(Calendar.MONTH, monthOfYear)
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    //inspired from layoutkotlin
    private fun showTimePickerDialog() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                // Update the class-level Calendar object
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDateTime.set(Calendar.MINUTE, minute)
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
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