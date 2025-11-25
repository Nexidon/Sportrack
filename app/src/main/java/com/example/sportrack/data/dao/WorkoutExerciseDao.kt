package com.example.sportrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sportrack.data.model.Exercise
import com.example.sportrack.data.model.WorkoutExercise

@Dao
interface WorkoutExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: WorkoutExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WorkoutExercise>)

    @Query("SELECT * FROM workout_exercises WHERE workoutId = :workoutId ORDER BY position")
    suspend fun getForWorkout(workoutId: Long): List<WorkoutExercise>

    // Простіша функція: отримати повні дані вправ для воркаута (JOIN)
    @Transaction
    @Query("""
        SELECT e.* FROM exercises e
        INNER JOIN workout_exercises we ON e.id = we.exerciseId
        WHERE we.workoutId = :workoutId
        ORDER BY we.position
    """)
    suspend fun getExercisesForWorkout(workoutId: Long): List<Exercise>

    @Query("DELETE FROM workout_exercises WHERE workoutId = :workoutId")
    suspend fun deleteByWorkout(workoutId: Long)
}
