package com.example.sportrack.ui.progress.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportrack.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.LinkedHashMap

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val weightDao = db.weightEntryDao()
    private val measurementDao = db.measurementEntryDao()
    private val strengthDao = db.strengthEntryDao()

    // Weight entries
    val allEntries: Flow<List<WeightEntry>> = weightDao.getAllEntries()

    // Measurements (from DB)
    val allMeasurements: Flow<List<MeasurementEntry>> = measurementDao.getAllEntries()

    // Strength entries
    val allStrengths: Flow<List<StrengthEntry>> = strengthDao.getAllEntries()

    // SharedPreferences для целей (можно хранить цели по разным ключам)
    private val prefs: SharedPreferences =
        application.getSharedPreferences("progress_prefs", Context.MODE_PRIVATE)

    // Флоу для основной цели по весу (legacy, чтобы UI, который полагается на .goal работал)
    private val _goal = MutableStateFlow<Double?>(prefs.getFloat("goal", -1f).takeIf { it >= 0 }?.toDouble())
    val goal = _goal.asStateFlow()

    fun saveGoal(value: Double) {
        _goal.value = value
        prefs.edit().putFloat("goal", value.toFloat()).apply()
    }

    // Универсальные функции для целей по произвольной метрике (ключ в prefs: "goal_<key>")
    fun saveGoalFor(key: String, value: Double) {
        prefs.edit().putFloat("goal_$key", value.toFloat()).apply()
    }

    fun getGoalFor(key: String): Double? {
        val v = prefs.getFloat("goal_$key", Float.NaN)
        return if (!v.isNaN()) v.toDouble() else null
    }

    // --- Weight methods ---
    fun saveWeight(weight: Double) {
        viewModelScope.launch {
            val newEntry = WeightEntry(date = System.currentTimeMillis(), weight = weight)
            weightDao.insert(newEntry)
        }
    }

    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            weightDao.deleteById(entry.id)
        }
    }

    fun updateWeight(entry: WeightEntry) {
        viewModelScope.launch {
            weightDao.update(entry)
        }
    }

    // --- Measurement methods ---
    fun saveMeasurement(entry: MeasurementEntry) {
        viewModelScope.launch {
            measurementDao.insert(entry)
        }
    }

    fun deleteMeasurement(entry: MeasurementEntry) {
        viewModelScope.launch {
            measurementDao.deleteById(entry.id)
        }
    }

    fun updateMeasurement(entry: MeasurementEntry) {
        viewModelScope.launch {
            measurementDao.update(entry)
        }
    }

    // --- Strength methods ---
    fun saveStrength(entry: StrengthEntry) {
        viewModelScope.launch {
            strengthDao.insert(entry)
        }
    }

    fun updateStrength(entry: StrengthEntry) {
        viewModelScope.launch {
            strengthDao.update(entry)
        }
    }

    fun deleteStrength(entry: StrengthEntry) {
        viewModelScope.launch {
            strengthDao.deleteById(entry.id)
        }
    }

    // ------------------------------
    // Функции для фильтрации по периоду (универсальная)
    // ------------------------------
    fun <T> filterByPeriod(entries: List<T>, period: Period, extractor: (T) -> Long): List<T> {
        if (period == Period.ALL) return entries
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        when (period) {
            Period.WEEK -> cal.add(Calendar.DAY_OF_YEAR, -7)
            Period.MONTH -> cal.add(Calendar.MONTH, -1)
            Period.YEAR -> cal.add(Calendar.YEAR, -1)
            else -> {}
        }
        val from = cal.timeInMillis
        return entries.filter { extractor(it) >= from && extractor(it) <= now }
    }

    // ------------------------------
    // Агрегаторы для силовых данных
    // ------------------------------

    // Возвращает для каждого liftType запись с максимальным весом (max weight)
    fun maxByLiftType(entries: List<StrengthEntry>): Map<String, StrengthEntry> {
        return entries.groupBy { it.liftType }
            .mapValues { (_, list) -> list.maxByOrNull { it.weight }!! }
    }

    // Суммарный рабочий тоннаж по liftType (weight * reps суммируется)
    fun sumTonnageByLiftType(entries: List<StrengthEntry>): Map<String, Double> {
        return entries.groupBy { it.liftType }
            .mapValues { (_, list) -> list.sumOf { it.weight * it.reps } }
    }

    // Агрегировать по дням: для построения столбиков по датам (например суммарный тоннаж в день)
    fun sumTonnageByDay(entries: List<StrengthEntry>): Map<Long, Double> {
        // Ключ — начало дня (timestamp в ms)
        val map = mutableMapOf<Long, Double>()
        val cal = Calendar.getInstance()
        for (e in entries) {
            cal.timeInMillis = e.date
            // обнуляем время (оставляем только дату)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            val ton = e.weight * e.reps
            map[dayStart] = (map[dayStart] ?: 0.0) + ton
        }
        // Возвращаем упорядоченную по дате карту
        return map.toList().sortedBy { it.first }.toMap()
    }

    // Утилиты для UI: получить веса/замеры/силовые для выбранного периода (suspend не нужен, т.к. входные данные обычно берутся из Flow и доступны в UI)
    fun filterWeightsForPeriod(entries: List<WeightEntry>, period: Period): List<WeightEntry> {
        return filterByPeriod(entries, period) { it.date }
    }

    fun filterMeasurementsForPeriod(entries: List<MeasurementEntry>, period: Period): List<MeasurementEntry> {
        return filterByPeriod(entries, period) { it.date }
    }

    fun filterStrengthsForPeriod(entries: List<StrengthEntry>, period: Period): List<StrengthEntry> {
        return filterByPeriod(entries, period) { it.date }
    }
}
