package com.example.derek_huang_myruns1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.derek_huang_myruns1.database.ExerciseEntry
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabase
import com.example.derek_huang_myruns1.database.ExerciseEntryDatabaseDao
import com.example.derek_huang_myruns1.database.ExerciseEntryListAdapter
import com.example.derek_huang_myruns1.database.ExerciseRepository
import com.example.derek_huang_myruns1.database.ExerciseViewModel
import com.example.derek_huang_myruns1.database.ExerciseViewModelFactory
import com.example.derek_huang_myruns1.database.OnEntryClickListener

class HistoryFragment : Fragment(), OnEntryClickListener {

    private lateinit var exerciseEntryAdapter: ExerciseEntryListAdapter
    private lateinit var viewModel: ExerciseViewModel
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModelFactory: ExerciseViewModelFactory
    private lateinit var databaseDao: ExerciseEntryDatabaseDao
    private lateinit var database: ExerciseEntryDatabase
    private lateinit var recyclerView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)

        // Obtain the ViewModel
        database = ExerciseEntryDatabase.getInstance(requireActivity())
        databaseDao = database.exerciseEntryDatabaseDao
        repository = ExerciseRepository(databaseDao)
        viewModelFactory = ExerciseViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ExerciseViewModel::class.java)

        // Setup RecyclerView
        exerciseEntryAdapter = ExerciseEntryListAdapter(this, requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = exerciseEntryAdapter

        //Observe LiveData
        viewModel.allEntries.observe(viewLifecycleOwner, { entries ->
            exerciseEntryAdapter.submitList(entries)
        })
        // Inflate the layout for this fragment
        return view
    }

    override fun onEntryClick(entry: ExerciseEntry) {
        Log.d("BUTTON CLICKED", "CLICKED ON ENTRY: ${entry.id}")

        when (entry.inputType) {
            0 -> { // Manual Entry
                val intent = Intent(context, EntryActivity::class.java).apply {
                    putExtra("ENTRY_ID", entry.id)
                }
                startActivity(intent)
            }
            1, 2 -> { // GPS or Automatic
                val intent = Intent(context, DisplayMapActivity::class.java).apply {
                    putExtra("ENTRY_ID", entry.id)
                    // If you have the list of LatLng points directly available, you can pass them here
                    // putExtra("LOCATION_LIST", entry.locationList)
                }
                startActivity(intent)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        // Refresh your list
        exerciseEntryAdapter.notifyDataSetChanged() // This will refresh the adapter data
    }

}