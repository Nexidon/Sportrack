package com.example.sportrack.data

import androidx.room.*

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(entity = WorkoutTemplate::class, parentColumns = ["id"], childColumns = ["workoutId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Exercise::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("workoutId"), Index("exerciseId")]
)
data class WorkoutExercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val position: Int = 0,     // порядок у воркауті
    val sets: Int? = null,     // якщо null — використовувати defaultSets з Exercise
    val reps: Int? = null,     // якщо null — використовувати defaultReps
    val durationSec: Int? = null // якщо timed — override defaultDurationSec
)
