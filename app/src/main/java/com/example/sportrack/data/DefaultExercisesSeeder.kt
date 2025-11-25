package com.example.sportrack.data

import android.content.Context
import com.example.sportrack.data.model.Exercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Простий seeder — додає прикладні вправи, якщо таблиця порожня.
 * Виклич один раз при старті додатку (наприклад у LaunchedEffect в MainActivity/TrainingScreen або в Application.onCreate).
 */

object DefaultExercisesSeeder {

    private val defaultExercises = listOf(
        // Грудні
        Exercise(
            name = "Жим лежачи штангою",
            primaryGroup = "Грудні м'язи",
            subgroup = "Середина",
            isTimed = false,
            defaultSets = 4,
            defaultReps = 6,
            restSec = 120,
            equipment = "Штанга"
        ),
        Exercise(
            name = "Жим гантелей під кутом",
            primaryGroup = "Грудні м'язи",
            subgroup = "Верх",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 8,
            restSec = 90,
            equipment = "Гантелі"
        ),
        Exercise(
            name = "Розводка гантелей лежачи",
            primaryGroup = "Грудні м'язи",
            subgroup = "Розведення",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 12,
            restSec = 60,
            equipment = "Гантелі"
        ),

        // Спина
        Exercise(
            name = "Тяга штанги в нахилі",
            primaryGroup = "Спина",
            subgroup = "Тяга",
            isTimed = false,
            defaultSets = 4,
            defaultReps = 6,
            restSec = 120,
            equipment = "Штанга"
        ),
        Exercise(
            name = "Тяга верхнього блоку",
            primaryGroup = "Спина",
            subgroup = "Ширина",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 10,
            restSec = 90,
            equipment = "Блок"
        ),
        Exercise(
            name = "Тяга гантелі одним плечем",
            primaryGroup = "Спина",
            subgroup = "Функціонал",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 10,
            restSec = 60,
            equipment = "Гантелі"
        ),

        // Ноги
        Exercise(
            name = "Присідання зі штангою",
            primaryGroup = "Ноги",
            subgroup = "Квадрицепс",
            isTimed = false,
            defaultSets = 4,
            defaultReps = 6,
            restSec = 120,
            equipment = "Штанга"
        ),
        Exercise(
            name = "Випади з гантелями",
            primaryGroup = "Ноги",
            subgroup = "Випади",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 10,
            restSec = 90,
            equipment = "Гантелі"
        ),
        Exercise(
            name = "Румунська тяга",
            primaryGroup = "Ноги",
            subgroup = "Гемстрінги",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 8,
            restSec = 90,
            equipment = "Штанга"
        ),

        // Плечі
        Exercise(
            name = "Армійський жим штангою",
            primaryGroup = "Плечі",
            subgroup = "Передні/середні",
            isTimed = false,
            defaultSets = 4,
            defaultReps = 6,
            restSec = 120,
            equipment = "Штанга"
        ),
        Exercise(
            name = "Жим гантелей сидячи",
            primaryGroup = "Плечі",
            subgroup = "Середні",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 8,
            restSec = 90,
            equipment = "Гантелі"
        ),
        Exercise(
            name = "Підйоми гантелей в сторони",
            primaryGroup = "Плечі",
            subgroup = "Бічні",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 12,
            restSec = 60,
            equipment = "Гантелі"
        ),

        // Біцепс
        Exercise(
            name = "Підйом штанги на біцепс",
            primaryGroup = "Біцепс",
            subgroup = "Штанга",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 8,
            restSec = 60,
            equipment = "Штанга"
        ),
        Exercise(
            name = "Підйом гантелей молоток",
            primaryGroup = "Біцепс",
            subgroup = "Молоток",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 10,
            restSec = 60,
            equipment = "Гантелі"
        ),
        Exercise(
            name = "Концентровані підйоми на біцепс",
            primaryGroup = "Біцепс",
            subgroup = "Ізоляція",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 12,
            restSec = 45,
            equipment = "Гантелі"
        ),

        // Трицепс
        Exercise(
            name = "Французький жим",
            primaryGroup = "Трицепс",
            subgroup = "Ізоляція",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 8,
            restSec = 60,
            equipment = "Штанга"
        ),
        Exercise(
            name = "Розгинання рук на блоці",
            primaryGroup = "Трицепс",
            subgroup = "Блок",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 12,
            restSec = 45,
            equipment = "Блок"
        ),
        Exercise(
            name = "Жим вузьким хватом",
            primaryGroup = "Трицепс",
            subgroup = "Жим",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 6,
            restSec = 90,
            equipment = "Штанга"
        ),

        // Прес
        Exercise(
            name = "Підйоми тулуба на прес",
            primaryGroup = "Прес",
            subgroup = "Верх",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 15,
            restSec = 45,
            equipment = "Мат"
        ),
        Exercise(
            name = "Планка",
            primaryGroup = "Прес",
            subgroup = "Статична",
            isTimed = true,
            defaultSets = 3,
            defaultDurationSec = 60,
            restSec = 45,
            equipment = "Мат"
        ),
        Exercise(
            name = "Підйоми ніг у висі",
            primaryGroup = "Прес",
            subgroup = "Нижній",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 12,
            restSec = 45,
            equipment = "Перекладина"
        ),

        // Ягодиці
        Exercise(
            name = "Степ-апи з гантелею",
            primaryGroup = "Ягодиці",
            subgroup = "Степ",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 12,
            restSec = 60,
            equipment = "Гантелі/Скамья"
        ),
        Exercise(
            name = "Тяга гантелі однією ногою",
            primaryGroup = "Ягодиці",
            subgroup = "Хіп",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 10,
            restSec = 60,
            equipment = "Гантелі"
        ),
        Exercise(
            name = "Місток (hip thrust)",
            primaryGroup = "Ягодиці",
            subgroup = "Місток",
            isTimed = false,
            defaultSets = 4,
            defaultReps = 8,
            restSec = 90,
            equipment = "Штанга/Гантелі"
        ),

        // Ікри
        Exercise(
            name = "Підйоми на носки стоячи",
            primaryGroup = "Ікри",
            subgroup = "Стоячи",
            isTimed = false,
            defaultSets = 4,
            defaultReps = 12,
            restSec = 60,
            equipment = "Тренажер/Степ"
        ),
        Exercise(
            name = "Сидячі підйоми на носки",
            primaryGroup = "Ікри",
            subgroup = "Сидячи",
            isTimed = false,
            defaultSets = 4,
            defaultReps = 15,
            restSec = 60,
            equipment = "Тренажер"
        ),

        // Передпліччя
        Exercise(
            name = "Зворотний підйом штанги на зап'ястя",
            primaryGroup = "Передпліччя",
            subgroup = "Зап'ястя",
            isTimed = false,
            defaultSets = 3,
            defaultReps = 15,
            restSec = 45,
            equipment = "Штанга"
        ),
        Exercise(
            name = "Захоплення молотком (farmer's walk) — коротка дистанція",
            primaryGroup = "Передпліччя",
            subgroup = "Статичне",
            isTimed = true,
            defaultSets = 3,
            defaultDurationSec = 30,
            restSec = 60,
            equipment = "Гантелі"
        )
    )

    /**
     * Неспеціальна асинхронна обгортка — викликай з UI (LaunchedEffect/CoroutineScope) або напряму:
     * DefaultExercisesSeeder.seedIfNeeded(context)
     */
    fun seedIfNeeded(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val dao = db.exerciseDao()
            val existing = dao.getAll()
            if (existing.isEmpty()) {
                dao.insertAll(defaultExercises)
            }
        }
    }
}
