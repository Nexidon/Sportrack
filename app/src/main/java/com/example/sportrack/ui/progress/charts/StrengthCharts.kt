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
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp).padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFFE5E5E5))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Немає даних для графіка", color = Color.Gray)
            }
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val axisColor = AndroidColor.parseColor("#999999")

    val sorted = remember(entries) { entries.sortedBy { it.date } }
    val labels = remember(sorted) { sorted.map { SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(it.date)) } }
    val lineEntries = remember(sorted) { sorted.mapIndexed { idx, e -> Entry(idx.toFloat(), e.weight.toFloat()) } }
    val trendEntries = remember(lineEntries, showTrend) {
        if (showTrend && lineEntries.size >= 2) calcMeasurementTrend(lineEntries) else emptyList()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    LineChart(ctx).apply {
                        description = Description().apply { text = "" }
                        legend.isEnabled = false
                        setBackgroundColor(AndroidColor.TRANSPARENT)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            textColor = axisColor
                            setDrawGridLines(false)
                            setDrawAxisLine(false)
                            granularity = 1f
                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    val idx = value.toInt()
                                    return if (idx in labels.indices) labels[idx] else ""
                                }
                            }
                        }

                        axisLeft.apply {
                            textColor = axisColor
                            gridColor = AndroidColor.parseColor("#F0F0F0")
                            setDrawAxisLine(false)
                            spaceTop = 20f
                            spaceBottom = 20f
                        }
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val dataSet = LineDataSet(lineEntries, (liftType ?: "Усі типи")).apply {
                        color = primaryColor
                        lineWidth = 3f
                        setDrawCircles(true)
                        setCircleColor(AndroidColor.WHITE)
                        circleRadius = 5f
                        circleHoleRadius = 3f
                        circleHoleColor = primaryColor
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawFilled(true)
                        fillColor = primaryColor
                        fillAlpha = 50
                        setDrawValues(false)
                    }

                    val sets = mutableListOf<ILineDataSet>(dataSet)

                    if (showTrend && trendEntries.isNotEmpty()) {
                        val trendSet = LineDataSet(trendEntries, "Тренд").apply {
                            color = AndroidColor.LTGRAY
                            lineWidth = 2f
                            enableDashedLine(10f, 10f, 0f)
                            setDrawCircles(false)
                            setDrawValues(false)
                            setDrawFilled(false)
                        }
                        sets.add(trendSet)
                    }

                    chart.axisLeft.removeAllLimitLines()
                    if (showGoal && liftType != null) {
                        val goal = viewModel.getGoalFor("lift_$liftType")
                        if (goal != null) {
                            val ll = LimitLine(goal.toFloat(), "Ціль").apply {
                                lineColor = AndroidColor.parseColor("#FFC107")
                                lineWidth = 2f
                                enableDashedLine(10f, 10f, 0f)
                                textColor = axisColor
                                textSize = 10f
                            }
                            chart.axisLeft.addLimitLine(ll)
                        }
                    }

                    chart.data = LineData(sets)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun StrengthBarProgress(entries: List<StrengthEntry>, viewModel: ProgressViewModel) {
    if (entries.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp).padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(2.dp, Color(0xFFE5E5E5))
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Немає даних для графіка", color = Color.Gray)
            }
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val axisColor = AndroidColor.parseColor("#999999")
    val tonnageByDay = remember(entries) { viewModel.sumTonnageByDay(entries) }

    val labels = remember(tonnageByDay) {
        tonnageByDay.keys.map { SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(it)) }
    }
    val values = remember(tonnageByDay) { tonnageByDay.values.toList() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    BarChart(ctx).apply {
                        description = Description().apply { text = "" }
                        legend.isEnabled = false
                        setBackgroundColor(AndroidColor.TRANSPARENT)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            textColor = axisColor
                            setDrawGridLines(false)
                            setDrawAxisLine(false)
                            granularity = 1f
                        }

                        axisLeft.apply {
                            textColor = axisColor
                            gridColor = AndroidColor.parseColor("#F0F0F0")
                            setDrawAxisLine(false)
                            spaceTop = 20f
                        }
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val entriesBar = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
                    val set = BarDataSet(entriesBar, "Тонаж за днями").apply {
                        valueTextSize = 10f
                        valueTextColor = axisColor
                        setDrawValues(true)
                        color = primaryColor
                    }
                    val data = BarData(set)
                    data.barWidth = 0.5f // Зробив стовпчики трохи тоншими для акуратності
                    chart.data = data

                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            return if (idx in labels.indices) labels[idx] else ""
                        }
                    }
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
fun StrengthChart(entries: List<StrengthEntry>, viewModel: ProgressViewModel) {
    if (entries.isEmpty()) return

    val tonnage = viewModel.sumTonnageByLiftType(entries)
    val labels = tonnage.keys.toList()
    val values = tonnage.values.toList()
    val axisColor = AndroidColor.parseColor("#999999")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    BarChart(ctx).apply {
                        description = Description().apply { text = "" }
                        legend.isEnabled = false
                        setBackgroundColor(AndroidColor.TRANSPARENT)
                        setScaleEnabled(false)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            textColor = axisColor
                            setDrawGridLines(false)
                            setDrawAxisLine(false)
                            granularity = 1f
                        }

                        axisLeft.apply {
                            textColor = axisColor
                            gridColor = AndroidColor.parseColor("#F0F0F0")
                            setDrawAxisLine(false)
                            spaceTop = 20f
                        }
                        axisRight.isEnabled = false
                    }
                },
                update = { chart ->
                    val entriesBar = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
                    val set = BarDataSet(entriesBar, "Тонаж").apply {
                        valueTextSize = 10f
                        valueTextColor = axisColor
                        setDrawValues(true)
                        color = Color(0xFF4CAF50).toArgb() // Зелений колір як і був
                    }
                    val data = BarData(set)
                    data.barWidth = 0.5f

                    chart.data = data

                    chart.xAxis.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val idx = value.toInt()
                            return if (idx in labels.indices) labels[idx] else ""
                        }
                    }
                    chart.invalidate()
                }
            )
        }
    }
}