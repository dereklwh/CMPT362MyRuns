package com.example.derek_huang_myruns1

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter



class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
//        super.onViewCreated(view, savedInstanceState)
//
//        //populate input type spinner
//
//        val inputTypeList = arrayOf("Manual Entry", "GPS", "Automatic")
//        val inputTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, inputTypeList)
//        inputTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//        // Set the adapter to the Spinner
//        input_type_spinner.adapter = inputTypeAdapter
//
//    }
}