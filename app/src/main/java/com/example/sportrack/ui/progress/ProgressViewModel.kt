package com.example.sportrack.ui.progress

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportrack.data.AppDatabase
import com.example.sportrack.data.model.MeasurementEntry
import com.example.sportrack.data.model.Period
import com.example.sportrack.data.model.StrengthEntry
import com.example.sportrack.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class ProgressViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.Companion.getDatabase(application)
    private val weightDao = db.weightEntryDao()
    private val measurementDao = db.measurementEntryDao()
    private val strengthDao = db.strengthEntryDao()

    // Weight entries
    val allEntries: Flow<List<WeightEntry>> = weightDao.getAllEntries()

    // Measurements (from DB)
    val allMeasurements: Flow<List<MeasurementEntry>> = measurementDao.getAllEntries()

    // Strength entries
    val allStrengths: Flow<List<StrengthEntry>> = strengthDao.getAllEntries()

    private val prefs: SharedPreferences =
        application.getSharedPreferences("progress_prefs", Context.MODE_PRIVATE)

    private val _goal =
        MutableStateFlow<Double?>(prefs.getFloat("goal", -1f).takeIf { it >= 0 }?.toDouble())
    val goal = _goal.asStateFlow()

    fun saveGoal(value: Double) {
        _goal.value = value
        prefs.edit().putFloat("goal", value.toFloat()).apply()
    }

    fun saveGoalFor(key: String, value: Double) {
        prefs.edit().putFloat("goal_$key", value.toFloat()).apply()
    }

    fun getGoalFor(key: String): Double? {
        val v = prefs.getFloat("goal_$key", Float.NaN)
        return if (!v.isNaN()) v.toDouble() else null
    }

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


    fun maxByLiftType(entries: List<StrengthEntry>): Map<String, StrengthEntry> {
        return entries.groupBy { it.liftType }
            .mapValues { (_, list) -> list.maxByOrNull { it.weight }!! }
    }

    fun sumTonnageByLiftType(entries: List<StrengthEntry>): Map<String, Double> {
        return entries.groupBy { it.liftType }
            .mapValues { (_, list) -> list.sumOf { it.weight * it.reps } }
    }

    fun sumTonnageByDay(entries: List<StrengthEntry>): Map<Long, Double> {
        val map = mutableMapOf<Long, Double>()
        val cal = Calendar.getInstance()
        for (e in entries) {
            cal.timeInMillis = e.date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val dayStart = cal.timeInMillis
            val ton = e.weight * e.reps
            map[dayStart] = (map[dayStart] ?: 0.0) + ton
        }
        return map.toList().sortedBy { it.first }.toMap()
    }

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