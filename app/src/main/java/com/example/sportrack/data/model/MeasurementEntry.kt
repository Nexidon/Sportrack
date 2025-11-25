package com.example.sportrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurement_entries")
data class MeasurementEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val chest: Double? = null,
    val waist: Double? = null,
    val hips: Double? = null,
    val biceps: Double? = null
)
