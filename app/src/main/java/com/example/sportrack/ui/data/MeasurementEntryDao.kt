package com.example.sportrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementEntryDao {
    @Insert
    suspend fun insert(entry: MeasurementEntry)

    @Update
    suspend fun update(entry: MeasurementEntry)

    @Query("DELETE FROM measurement_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM measurement_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<MeasurementEntry>>
}
