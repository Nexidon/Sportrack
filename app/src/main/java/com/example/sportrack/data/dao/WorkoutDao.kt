package com.example.sportrack.data.dao

import androidx.room.*
import com.example.sportrack.data.model.WorkoutTemplate

/**
 * Об'єкт доступу до даних (DAO) для роботи з шаблонами тренувань.
 * Забезпечує методи для збереження, отримання та видалення тренувальних планів.
 */
@Dao
interface WorkoutDao {

    /**
     * Зберігає новий шаблон тренування або оновлює існуючий (при збігу ID).
     *
     * @param workout Об'єкт шаблону тренування для збереження.
     * @return ID вставленого або оновленого запису.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workout: WorkoutTemplate): Long

    /**
     * Отримує всі шаблони тренувань, відсортовані за датою створення (найновіші перші).
     *
     * @return Список доступних шаблонів [WorkoutTemplate].
     */
    @Query("SELECT * FROM workout_templates ORDER BY createdAt DESC")
    suspend fun getAll(): List<WorkoutTemplate>

    /**
     * Отримує конкретний шаблон тренування за його унікальним ідентифікатором.
     *
     * @param id Унікальний ідентифікатор шаблону.
     * @return Об'єкт [WorkoutTemplate], або null, якщо такого шаблону не знайдено.
     */
    @Query("SELECT * FROM workout_templates WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WorkoutTemplate?

    /**
     * Видаляє вибраний шаблон тренування з бази даних.
     *
     * @param workout Об'єкт шаблону, який потрібно видалити.
     */
    @Delete
    suspend fun delete(workout: WorkoutTemplate)
}