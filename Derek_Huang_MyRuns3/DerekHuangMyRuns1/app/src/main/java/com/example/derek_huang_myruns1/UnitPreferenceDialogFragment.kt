package com.example.derek_huang_myruns1

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager

class UnitPreferenceDialogFragment : DialogFragment() {
    private lateinit var radioGroup: RadioGroup

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_unit_preference_dialog, null)

        radioGroup = view.findViewById(R.id.radioGroupUnit) // Ensure this ID matches your layout

        // Shared preferences to remember what is clicked
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val currentUnit = sharedPreferences.getString("unit_preference", "Metric") // "Metric" as default
        if (currentUnit == "Imperial") {
            radioGroup.check(R.id.radioButtonImperial)
        } else {
            radioGroup.check(R.id.radioButtonMetric)
        }

        builder.setView(view)
        builder.setTitle("Unit Preference")
        builder.setPositiveButton("OK") { _, _ ->
            // Save the selected unit when the user clicks OK
            val selectedUnit = if (radioGroup.checkedRadioButtonId == R.id.radioButtonMetric) "Metric" else "Imperial"
            with(sharedPreferences.edit()) {
                putString("unit_preference", selectedUnit)
                apply()
            }
            Toast.makeText(activity, "Unit preference saved: $selectedUnit", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("CANCEL", null)

        return builder.create()
    }
}
