package com.example.sportrack.ui.progress.charts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sportrack.data.model.StrengthEntry
import com.example.sportrack.ui.progress.ProgressViewModel
import com.example.sportrack.util.calcMeasurementTrend
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StrengthLineProgress(
    entries: List<StrengthEntry>,
    liftType: String?,
    showTrend: Boolean,
    showGoal: Boolean,
    viewModel: ProgressViewModel
) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text("Немає даних для графіка")
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val tertiaryColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    val sorted = remember(entries) { entries.sortedBy { it.date } }
    val labels = remember(sorted) { sorted.map { SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date(it.date)) } }
    val lineEntries = remember(sorted) { sorted.mapIndexed { idx, e -> Entry(idx.toFloat(), e.weight.toFloat()) } }
    val trendEntries = remember(lineEntries, showTrend) {
        if (showTrend && lineEntries.size >= 2) calcMeasurementTrend(lineEntries) else emptyList()
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(8.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                description = Description().apply { text = "" }
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisRight.isEnabled = false
                legend.isEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(lineEntries, (liftType ?: "Усі типи")).apply {
                setDrawValues(false)
                setDrawCircles(true)
                lineWidth = 2f
                color = primaryColor
                setCircleColor(primaryColor)
            }

            val sets = mutableListOf<ILineDataSet>(dataSet)

            if (showTrend && trendEntries.isNotEmpty()) {
                val trendSet = LineDataSet(trendEntries, "Тренд").apply {
                    setDrawCircles(false)
                    lineWidth = 2f
                    color = secondaryColor
                    setDrawValues(false)
                }
                sets.add(trendSet)
            }

            chart.data = LineData(sets)

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in labels.indices) labels[idx] else ""
                }
            }
            chart.xAxis.setLabelCount(labels.size.coerceAtMost(6), true)
            chart.axisLeft.setDrawGridLines(false)
            chart.xAxis.setDrawGridLines(false)

            if (showGoal && liftType != null) {
                val goal = viewModel.getGoalFor("lift_$liftType")
                if (goal != null) {
                    chart.axisLeft.removeAllLimitLines()
                    val ll = LimitLine(goal.toFloat(), "Ціль").apply {
                        lineWidth = 2f
                        lineColor = tertiaryColor
                        textColor = onSurfaceColor
                    }
                    chart.axisLeft.addLimitLine(ll)
                    val yMin = (lineEntries.minOfOrNull { it.y } ?: 0f).coerceAtMost(goal.toFloat()) - 1f
                    val yMax = (lineEntries.maxOfOrNull { it.y } ?: 0f).coerceAtLeast(goal.toFloat()) + 1f
                    chart.axisLeft.axisMinimum = yMin
                    chart.axisLeft.axisMaximum = yMax
                }
            } else {
                chart.axisLeft.removeAllLimitLines()
            }

            chart.invalidate()
        }
    )
}

@Composable
fun StrengthBarProgress(entries: List<StrengthEntry>, viewModel: ProgressViewModel) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text("Немає даних для графіка")
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val tonnageByDay = remember(entries) {
        viewModel.sumTonnageByDay(entries)
    }

    val labels = remember(tonnageByDay) {
        tonnageByDay.keys.map { SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(it)) }
    }
    val values = remember(tonnageByDay) { tonnageByDay.values.toList() }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(8.dp),
        factory = { ctx ->
            BarChart(ctx).apply {
                description = Description().apply { text = "" }
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                legend.isEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { chart ->
            val entriesBar = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
            val set = BarDataSet(entriesBar, "Тонаж за днями").apply {
                valueTextSize = 10f
                setDrawValues(true)
                color = primaryColor
            }
            val data = BarData(set)
            data.barWidth = 0.7f
            chart.data = data

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in labels.indices) labels[idx] else ""
                }
            }

            chart.xAxis.setDrawGridLines(false)
            chart.axisLeft.setDrawGridLines(false)
            chart.invalidate()
        }
    )
}

@Composable
fun StrengthChart(entries: List<StrengthEntry>, viewModel: ProgressViewModel) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text("Немає даних для графіка")
        }
        return
    }

    val tonnage = viewModel.sumTonnageByLiftType(entries)
    val labels = tonnage.keys.toList()
    val values = tonnage.values.toList()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(8.dp),
        factory = { ctx ->
            BarChart(ctx).apply {
                description = Description().apply { text = "" }
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                legend.isEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { chart ->
            val entriesBar = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
            val set = BarDataSet(entriesBar, "Тонаж").apply {
                valueTextSize = 10f
                setDrawValues(true)
                color = Color(0xFF4CAF50).toArgb()
            }
            val data = BarData(set)
            data.barWidth = 0.7f

            chart.data = data

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in labels.indices) labels[idx] else ""
                }
            }
            chart.xAxis.setDrawGridLines(false)
            chart.axisLeft.setDrawGridLines(false)

            chart.invalidate()
        }
    )
}