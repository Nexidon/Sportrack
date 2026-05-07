package com.example.sportrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val exerciseName: String,
    val weight: Double,
    val reps: Int,
    val sets: Int
)