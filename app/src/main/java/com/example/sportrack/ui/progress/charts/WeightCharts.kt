package com.example.sportrack.ui.progress.charts

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sportrack.data.model.WeightEntry
import com.example.sportrack.ui.theme.SportGreen
import com.example.sportrack.util.calcTrend
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeightChart(
    entries: List<WeightEntry>,
    goal: Double?,
    showTrend: Boolean
) {
    if (entries.isEmpty()) return

    // 1. ИСПРАВЛЕНИЕ "ЗАДОМ НАПЕРЕД"
    // Сортируем от старых к новым (чтобы график рисовался слева направо)
    val sortedEntries = remember(entries) { entries.sortedBy { it.date } }

    // Подготовка данных для графика
    val chartDataPoints = remember(sortedEntries) {
        sortedEntries.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.weight.toFloat())
        }
    }

    // Подготовка подписей дат (чтобы снизу были даты, а не цифры 0, 1, 2)
    val dateLabels = remember(sortedEntries) {
        val format = SimpleDateFormat("dd.MM", Locale.getDefault())
        sortedEntries.map { format.format(Date(it.date)) }
    }

    // Цвета из нашей темы (конвертируем в Android Color Int)
    val mainColor = SportGreen.toArgb()
    val axisColor = AndroidColor.parseColor("#999999") // Серый для текста

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    LineChart(context).apply {
                        // 2. ДИЗАЙН ГРАФИКА
                        description.isEnabled = false
                        legend.isEnabled = false
                        setBackgroundColor(AndroidColor.TRANSPARENT) // Убираем черный фон
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false) // Отключаем зум, чтобы не ломать вид
                        setDrawGridBackground(false)

                        // Настройка оси X (Снизу)
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            textColor = axisColor
                            setDrawGridLines(false) // Убираем вертикальную сетку
                            setDrawAxisLine(false)
                            granularity = 1f
                            // Ставим наши красивые даты
                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    val index = value.toInt()
                                    return if (index in dateLabels.indices) dateLabels[index] else ""
                                }
                            }
                        }

                        // Настройка оси Y (Слева)
                        axisLeft.apply {
                            textColor = axisColor
                            gridColor = AndroidColor.parseColor("#F0F0F0") // Очень легкая сетка
                            setDrawAxisLine(false)
                            // Добавляем отступы сверху и снизу, чтобы линия не прилипала
                            spaceTop = 20f
                            spaceBottom = 20f
                        }
                        axisRight.isEnabled = false // Убираем цифры справа
                    }
                },
                update = { chart ->
                    // 3. НАСТРОЙКА ЛИНИИ
                    val dataSet = LineDataSet(chartDataPoints, "Вага").apply {
                        color = mainColor
                        lineWidth = 3f // Жирная линия

                        // ДЕЛАЕМ КРАСИВЫЕ КРУГИ
                        setDrawCircles(true)
                        setCircleColor(AndroidColor.WHITE) // Белая середина
                        circleRadius = 5f
                        circleHoleRadius = 3f
                        circleHoleColor = mainColor // Цветная обводка (пончик)

                        // ГЛАДКАЯ ЛИНИЯ (Вместо острых углов)
                        mode = LineDataSet.Mode.CUBIC_BEZIER

                        // ЗАЛИВКА ПОД ГРАФИКОМ
                        setDrawFilled(true)
                        fillColor = mainColor
                        fillAlpha = 50 // Прозрачность заливки

                        setDrawValues(false) // Убираем цифры над точками (мусор)
                    }

                    // Линия тренда (если включена)
                    val sets = mutableListOf(dataSet)

                    if (showTrend && entries.size >= 2) {
                        val trendPoints = calcTrend(sortedEntries)
                        val trendSet = LineDataSet(trendPoints, "Тренд").apply {
                            color = AndroidColor.LTGRAY
                            lineWidth = 2f
                            enableDashedLine(10f, 10f, 0f) // Пунктир
                            setDrawCircles(false)
                            setDrawValues(false)
                            setDrawFilled(false)
                        }
                        sets.add(trendSet)
                    }

                    // Линия цели (если есть)
                    chart.axisLeft.removeAllLimitLines()
                    if (goal != null) {
                        val limitLine = LimitLine(goal.toFloat(), "Ціль").apply {
                            lineColor = AndroidColor.parseColor("#FFC107") // Желтый цвет
                            lineWidth = 2f
                            enableDashedLine(10f, 10f, 0f)
                            textColor = axisColor
                            textSize = 10f
                        }
                        chart.axisLeft.addLimitLine(limitLine)
                    }

                    chart.data = LineData(sets.toList())
                    chart.invalidate() // Перерисовать
                }
            )
        }
    }
}