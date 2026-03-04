package com.example.sportrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val primaryGroup: String,
    val subgroup: String? = null,
    val isTimed: Boolean = false,
    val defaultSets: Int? = null,
    val defaultReps: Int? = null,
    val defaultDurationSec: Int? = null,
    val restSec: Int? = null,
    val equipment: String? = null
)
