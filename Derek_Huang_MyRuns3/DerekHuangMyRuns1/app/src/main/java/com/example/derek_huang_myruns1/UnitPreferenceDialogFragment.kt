package com.example.derek_huang_myruns1

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class UnitPreferenceDialogFragment : DialogFragment(), DialogInterface.OnClickListener{
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog

        val builder = AlertDialog.Builder(requireActivity())
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_unit_preference_dialog, null)
        builder.setView(view)
        builder.setTitle("Unit Preference")
        builder.setNegativeButton("CANCEL", this)
        ret = builder.create()
        return ret
    }

    override fun onClick(dialogInterface: DialogInterface, id: Int){
        return
    }
}