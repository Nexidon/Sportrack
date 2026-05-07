package com.example.sportrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sportrack.data.dao.DayAssignmentDao
import com.example.sportrack.data.dao.ExerciseDao
import com.example.sportrack.data.dao.MeasurementEntryDao
import com.example.sportrack.data.dao.StrengthEntryDao
import com.example.sportrack.data.dao.WeightEntryDao
import com.example.sportrack.data.dao.WorkoutDao
import com.example.sportrack.data.dao.WorkoutExerciseDao
import com.example.sportrack.data.dao.WorkoutLogDao
import com.example.sportrack.data.model.DayAssignment
import com.example.sportrack.data.model.Exercise
import com.example.sportrack.data.model.MeasurementEntry
import com.example.sportrack.data.model.StrengthEntry
import com.example.sportrack.data.model.WeightEntry
import com.example.sportrack.data.model.WorkoutExercise
import com.example.sportrack.data.model.WorkoutTemplate
import com.example.sportrack.data.model.WorkoutLog

@Database(
    entities = [
        WeightEntry::class,
        MeasurementEntry::class,
        StrengthEntry::class,
        DayAssignment::class,
        Exercise::class,
        WorkoutTemplate::class,
        WorkoutExercise::class,
        WorkoutLog::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun measurementEntryDao(): MeasurementEntryDao
    abstract fun strengthEntryDao(): StrengthEntryDao

    abstract fun dayAssignmentDao(): DayAssignmentDao

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun workoutLogDao(): WorkoutLogDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}