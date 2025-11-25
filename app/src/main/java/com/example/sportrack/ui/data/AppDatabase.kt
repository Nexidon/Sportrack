package com.example.sportrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        WeightEntry::class,
        MeasurementEntry::class,
        StrengthEntry::class,
        DayAssignment::class,
        Exercise::class,
        WorkoutTemplate::class,
        WorkoutExercise::class
    ],
    version = 6, // підняти версію (було 5)
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

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "app_database")
                    .fallbackToDestructiveMigration() // для розробки OK — у продакшені робити міграції
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
