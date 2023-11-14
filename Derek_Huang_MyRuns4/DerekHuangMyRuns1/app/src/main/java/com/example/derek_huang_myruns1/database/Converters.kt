package com.example.derek_huang_myruns1.database

import androidx.room.TypeConverter
import java.util.Calendar

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Calendar? {
        return value?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }
    }

    @TypeConverter
    fun calendarToTimestamp(calendar: Calendar?): Long? {
        return calendar?.timeInMillis
    }
    companion object {
        fun milesToKilometers(miles: Double): Double = miles * 1.60934
    }

}
