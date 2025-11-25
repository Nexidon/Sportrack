package com.example.sportrack.data

import androidx.room.*

@Dao
interface ExerciseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: Exercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Exercise>)

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Exercise?

    @Query("SELECT * FROM exercises ORDER BY name")
    suspend fun getAll(): List<Exercise>

    @Query("SELECT * FROM exercises WHERE primaryGroup = :group ORDER BY name")
    suspend fun getByPrimaryGroup(group: String): List<Exercise>

    @Query("SELECT * FROM exercises WHERE primaryGroup IN(:groups) ORDER BY name")
    suspend fun getByGroups(groups: List<String>): List<Exercise>

    @Delete
    suspend fun delete(exercise: Exercise)
}
