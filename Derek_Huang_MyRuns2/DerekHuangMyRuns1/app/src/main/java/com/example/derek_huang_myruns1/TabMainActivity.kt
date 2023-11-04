package com.example.derek_huang_myruns1

import SettingsFragment
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceFragmentCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class TabMainActivity : AppCompatActivity(){

    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tab_main)

        setUpTabs()
    }

    private fun setUpTabs() {
        viewPager2 = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tablayout)

        //initalize fragments
        val startFragment = StartFragment()
        val historyFragment = HistoryFragment()
        val settingsFragment = SettingsFragment()

        //add fragments to list
        var fragments = ArrayList<Fragment>()
        fragments.add(startFragment)
        fragments.add(historyFragment)
        fragments.add(settingsFragment)

        //create and set up adapter
        val myFragmentStateAdapter = MyFragmentStateAdapter(this, fragments)
        viewPager2.adapter = myFragmentStateAdapter

        val tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "Start"
                1 -> tab.text = "History"
                2 -> tab.text = "Settings"
            }
        }.attach()
    }

    fun onStartButtonClicked(view: View?) {
        val inputTypeSpinner = findViewById<Spinner>(R.id.input_type_spinner)
        val selectedInputType = inputTypeSpinner.selectedItem.toString()

        if (selectedInputType == "Manual Entry") {
            val manualEntryIntent = Intent(this, ManualEntryActivity::class.java)
            startActivity(manualEntryIntent)
        } else if (selectedInputType == "Automatic" || selectedInputType == "GPS") {
            val mapIntent = Intent(this, MapActivity::class.java)
            startActivity(mapIntent)
        }
    }
}