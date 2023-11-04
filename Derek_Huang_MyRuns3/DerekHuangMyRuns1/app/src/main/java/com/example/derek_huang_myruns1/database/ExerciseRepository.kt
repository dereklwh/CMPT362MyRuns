package com.example.derek_huang_myruns1.database

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ExerciseRepository(private val exerciseEntryDatabaseDao: ExerciseEntryDatabaseDao) {
    // The repository manages queries and allows you to use multiple backends.
    // Room executes all queries on a separate thread.

    // Observed LiveData will notify the observer when the data has changed.
    val allEntries: LiveData<List<ExerciseEntry>> = exerciseEntryDatabaseDao.getAllEntries()


    suspend fun insertEntry(exerciseEntry: ExerciseEntry) {
        CoroutineScope(IO).launch{
            exerciseEntryDatabaseDao.insertEntry(exerciseEntry)

        }
    }

//    suspend fun deleteEntry(key: Long) {
//            exerciseEntryDatabaseDao.deleteEntry(key)
//    }
}

