package com.example.derek_huang_myruns1

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class ManualEntryActivity : AppCompatActivity() {
    private val info = arrayOf(
        "Date", "Time", "Duration", "Distance", "Calories", "Heart Rate", "Comment"
    )
    private lateinit var myListView: ListView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private val calendar = java.util.Calendar.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_entry)

        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        myListView = findViewById(R.id.myListView)

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
            //TODO: use in future lab
            val enteredText = input.text.toString()
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
            //TODO: use in future lab
            val enteredText = input.text.toString()
        }


        builder.setNegativeButton("CANCEL") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}