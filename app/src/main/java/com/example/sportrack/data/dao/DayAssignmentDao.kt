package com.example.sportrack.data.dao

import androidx.room.*
import com.example.sportrack.data.model.DayAssignment

@Dao
interface DayAssignmentDao {
    @Query("SELECT * FROM day_assignments")
    suspend fun getAll(): List<DayAssignment>

    @Query("SELECT * FROM day_assignments WHERE day = :day LIMIT 1")
    suspend fun getByDay(day: String): DayAssignment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: DayAssignment)

    @Query("DELETE FROM day_assignments WHERE day = :day")
    suspend fun deleteByDay(day: String)
}
