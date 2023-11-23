package com.example.derek_huang_myruns1.database

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.derek_huang_myruns1.R
import kotlinx.coroutines.NonDisposableHandle.parent

interface OnEntryClickListener {
    fun onEntryClick(entry: ExerciseEntry)

}
class ExerciseEntryListAdapter(private val onEntryClickListener: OnEntryClickListener, context: Context
) : androidx.recyclerview.widget.ListAdapter<ExerciseEntry, ExerciseEntryListAdapter.ExerciseEntryViewHolder>(ExerciseEntryDiffCallback()) {

    private var sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    private var unitPreference: String = sharedPreferences.getString("unit_preference", "Metric") ?: "Metric"


    class ExerciseEntryViewHolder(itemView: View, val unitPreference: String) : RecyclerView.ViewHolder(itemView) {
        private val entryTextView: TextView = itemView.findViewById(R.id.tv_entry)


        fun bind(entry: ExerciseEntry) {
            val formattedDateTime = entry.getFormattedDateTime()
            val entryTypeString = entry.getEntryTypeString()
            val activityTypeString = entry.getActivityTypeString()
            val formattedDuration = entry.getFormattedDuration()

            // Determine the distance based on the preference
            val distance = if (unitPreference == "Metric") {
                String.format("%.2f Kilometers", entry.distance)
            } else {
                val distanceInMiles = Converters.kilometersToMiles(entry.distance)
                String.format("%.2f Miles", distanceInMiles)
            }

            entryTextView.text = "$entryTypeString, $activityTypeString, $formattedDateTime\n$distance, $formattedDuration"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseEntryViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.history_layout_adapter, parent, false)
        return ExerciseEntryViewHolder(view, unitPreference)
    }

    override fun onBindViewHolder(holder: ExerciseEntryViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry)
        holder.itemView.setOnClickListener {
            onEntryClickListener.onEntryClick(entry)
        }
    }
}

class ExerciseEntryDiffCallback : DiffUtil.ItemCallback<ExerciseEntry>() {
    override fun areItemsTheSame(oldItem: ExerciseEntry, newItem: ExerciseEntry): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: ExerciseEntry, newItem: ExerciseEntry): Boolean {
        return oldItem == newItem
    }
}