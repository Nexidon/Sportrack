package com.example.sportrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_assignments")
data class DayAssignment(
    @PrimaryKey val day: String,
    val groups: String // храним через запятую: "Спина,Ноги,Біцепс"
)
