package com.example.derek_huang_myruns1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MapActivity : AppCompatActivity() {

    lateinit var saveButton : Button
    lateinit var cancelButton : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        //Button Functionality (just close activity for now)
        cancelButton.setOnClickListener {
            finish()
        }
        //TODO: save entries for mylab3
        saveButton.setOnClickListener {
            finish()
        }
    }
}