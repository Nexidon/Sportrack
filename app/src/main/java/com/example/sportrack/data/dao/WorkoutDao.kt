package com.example.sportrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.sportrack.data.model.WorkoutTemplate

@Dao
interface WorkoutDao {
    // Твои методы для работы с WorkoutTemplate
    @Insert
    suspend fun insert(workout: WorkoutTemplate)

    @Query("SELECT * FROM workout_templates")
    suspend fun getAll(): List<WorkoutTemplate>
}