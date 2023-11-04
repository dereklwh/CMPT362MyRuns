package com.example.derek_huang_myruns1.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExerciseEntryDatabaseDao {
    @Insert
    fun insertEntry(exerciseEntry: ExerciseEntry)
    @Query("SELECT * FROM exercise_table")
    fun getAllEntries(): LiveData<List<ExerciseEntry>>

//    @Query("DELETE FROM exercise_table WHERE id = :key")
//    suspend fun deleteEntry(key: Long)

}