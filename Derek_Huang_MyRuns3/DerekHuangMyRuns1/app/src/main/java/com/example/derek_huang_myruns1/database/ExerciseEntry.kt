package com.example.derek_huang_myruns1.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "exercise_table")
data class ExerciseEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val inputType: Int,
    val activityType: Int,
    val dateTime: Calendar,
    val duration: Double,
    val distance: Double,
    val heartRate: Double,
    val comment: String,
)