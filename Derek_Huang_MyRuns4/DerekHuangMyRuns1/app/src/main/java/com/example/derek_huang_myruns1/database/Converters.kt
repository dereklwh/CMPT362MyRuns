package com.example.derek_huang_myruns1.database

import android.os.Parcel
import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.sql.Blob
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
        fun kilometersToMiles(kilometers: Double): Double {
            return kilometers / 1.60934
        }

        fun milesToKilometers(miles: Double): Double {
            return miles * 1.60934
        }
    }

    @TypeConverter
    fun fromLatLngList(locationList: ArrayList<LatLng>?): ByteArray? {
        if (locationList == null) return null

        val byteStream = ByteArrayOutputStream()
        try {
            ObjectOutputStream(byteStream).use { oos ->
                oos.writeObject(locationList.map { Pair(it.latitude, it.longitude) })
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return byteStream.toByteArray()
    }

    @TypeConverter
    fun toLatLngList(byteArray: ByteArray?): ArrayList<LatLng>? {
        if (byteArray == null) return null

        val byteStream = ByteArrayInputStream(byteArray)
        return try {
            ObjectInputStream(byteStream).use { ois ->
                @Suppress("UNCHECKED_CAST")
                (ois.readObject() as List<Pair<Double, Double>>)
                    .map { LatLng(it.first, it.second) }
                    .let { ArrayList(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
