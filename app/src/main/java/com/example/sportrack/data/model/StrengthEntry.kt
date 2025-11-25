package com.example.sportrack.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "strength_entries")
data class StrengthEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val liftType: String, // например "Bench", "Squat", "Deadlift" или "Pull-up"
    val weight: Double, // рабочий вес
    val reps: Int
)