package com.example.sportrack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sportrack.data.model.WorkoutLog

@Dao
interface WorkoutLogDao {
    @Insert
    suspend fun insertLog(log: WorkoutLog)

    @Delete
    suspend fun deleteLog(log: WorkoutLog)

    @Query("SELECT * FROM workout_logs WHERE exerciseName = :name ORDER BY date ASC")
    suspend fun getLogsForExercise(name: String): List<WorkoutLog>

    @Query("SELECT * FROM workout_logs WHERE exerciseName = :name ORDER BY date DESC LIMIT 1")
    suspend fun getLastLogForExercise(name: String): WorkoutLog?

    @Query("SELECT * FROM workout_logs ORDER BY date DESC")
    suspend fun getAllLogs(): List<WorkoutLog>
}