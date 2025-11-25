package com.example.sportrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long, // Используем timestamp (Long) для даты
    val weight: Double
)