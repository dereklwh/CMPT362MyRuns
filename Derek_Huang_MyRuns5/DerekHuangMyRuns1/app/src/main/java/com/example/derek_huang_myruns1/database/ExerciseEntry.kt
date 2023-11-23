package com.example.derek_huang_myruns1.database

import android.icu.text.SimpleDateFormat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng
import java.sql.Blob
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "exercise_table")
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val inputType: Int,
    val activityType: Int,
    val dateTime: Calendar,
    val duration: Double, //seconds
    val distance: Double, //kms
    val calories: Double,
    val heartRate: Double,
    val comment: String,
    val locationList: ArrayList<LatLng> = arrayListOf(),
    val avgPace: Double,
    val avgSpeed: Double,
    val climb: Double
) {

    // Add a method to format the date and time
    fun getFormattedDateTime(): String {
        val formatter = SimpleDateFormat("EEE, MMM d, yyyy h:mm a", Locale.getDefault())
        return formatter.format(dateTime.time)
    }
    fun getEntryTypeString(): String {
        return when (inputType) {
            0 -> "Manual Entry"
            1 -> "GPS"
            2 -> "Automatic"
            else -> "Unknown"
        }
    }

    // Get the String value for Activity Type
    fun getActivityTypeString(): String {
        return when (activityType) {
            0 -> "Running"
            1 -> "Walking"
            2 -> "Standing"
            3 -> "Cycling"
            4 -> "Hiking"
            5 -> "Downhill Skiing"
            6 -> "Cross-Country Skiing"
            7 -> "Snowboarding"
            8 -> "Skating"
            9 -> "Swimming"
            10 -> "Mountain Biking"
            11 -> "Wheelchair"
            12 -> "Elliptical"
            13 -> "Other"
            else -> "Unknown"
        }
    }
    //format duration to mins and secs
    fun getFormattedDuration(): String {
        val minutes = duration.toInt() / 60
        val seconds = duration.toInt() % 60

        return String.format("%02d mins %02d secs", minutes, seconds)
    }
}
