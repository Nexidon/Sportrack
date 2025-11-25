package com.example.sportrack.data.dao

import androidx.room.*
import com.example.sportrack.data.model.WorkoutTemplate

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutTemplate): Long

    @Query("SELECT * FROM workout_templates ORDER BY createdAt DESC")
    suspend fun getAll(): List<WorkoutTemplate>

    @Query("SELECT * FROM workout_templates WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WorkoutTemplate?

    @Delete
    suspend fun delete(workout: WorkoutTemplate)
}
