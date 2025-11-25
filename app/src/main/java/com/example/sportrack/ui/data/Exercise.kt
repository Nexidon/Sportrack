package com.example.sportrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                 // назва вправи
    val primaryGroup: String,         // основна група (наприклад "Грудні м'язи")
    val subgroup: String? = null,     // підгрупа/тип (наприклад "Верхня частина")
    val isTimed: Boolean = false,     // true -> duration використовується замість reps
    val defaultSets: Int? = null,
    val defaultReps: Int? = null,
    val defaultDurationSec: Int? = null,
    val restSec: Int? = null,         // відпочинок між сетами (с)
    val equipment: String? = null     // обладнання (гантелі, штанга, машина...)
)
