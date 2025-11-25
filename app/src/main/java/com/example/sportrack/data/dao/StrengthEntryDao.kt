package com.example.sportrack.data.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.sportrack.data.model.StrengthEntry
import kotlinx.coroutines.flow.Flow


@Dao
interface StrengthEntryDao {
    @Insert
    suspend fun insert(entry: StrengthEntry)


    @Update
    suspend fun update(entry: StrengthEntry)


    @Query("DELETE FROM strength_entries WHERE id = :id")
    suspend fun deleteById(id: Int)


    @Query("SELECT * FROM strength_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<StrengthEntry>>
}