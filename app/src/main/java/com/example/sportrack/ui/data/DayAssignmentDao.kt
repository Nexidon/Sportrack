package com.example.sportrack.data

import androidx.room.*

@Dao
interface DayAssignmentDao {
    @Query("SELECT * FROM day_assignments")
    suspend fun getAll(): List<DayAssignment>

    @Query("SELECT * FROM day_assignments WHERE day = :day LIMIT 1")
    suspend fun getByDay(day: String): DayAssignment?   // <-- додано

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: DayAssignment)

    @Query("DELETE FROM day_assignments WHERE day = :day")
    suspend fun deleteByDay(day: String)
}
