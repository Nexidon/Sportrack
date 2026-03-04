package com.example.sportrack.ui.progress.charts

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sportrack.data.model.MeasurementEntry
import com.example.sportrack.util.calcMeasurementTrend
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

@Composable
fun MeasurementChart(
    measurements: List<MeasurementEntry>,
    valueSelector: (MeasurementEntry) -> Double?,
    label: String,
    lineColor: Color,
    showTrend: Boolean = false,
    goal: Double? = null
) {
    if (measurements.isEmpty()) return

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(8.dp),
        factory = { context -> LineChart(context).apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            axisRight.isEnabled = false

            setBackgroundColor(android.graphics.Color.BLACK)
            xAxis.textColor = android.graphics.Color.WHITE
            axisLeft.textColor = android.graphics.Color.WHITE
            xAxis.gridColor = android.graphics.Color.DKGRAY
            axisLeft.gridColor = android.graphics.Color.DKGRAY
            legend.textColor = android.graphics.Color.WHITE
        } },
        update = { chart ->
            val entries = measurements.mapIndexedNotNull { index, entry -> valueSelector(entry)?.let { Entry(index.toFloat(), it.toFloat()) } }
            if (entries.isEmpty()) return@AndroidView

            val dataSet = LineDataSet(entries, label).apply {
                mode = LineDataSet.Mode.LINEAR
                color = lineColor.toArgb()
                setCircleColor(lineColor.toArgb())
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
            }

            val sets = mutableListOf<ILineDataSet>(dataSet)

            goal?.let {
                val goalEntries = listOf(
                    Entry(0f, it.toFloat()),
                    Entry(entries.size - 1f, it.toFloat())
                )
                val goalSet = LineDataSet(goalEntries, "Ціль").apply {
                    color = Color.Red.toArgb()
                    lineWidth = 2f
                    setDrawCircles(false)
                    enableDashedLine(10f, 5f, 0f)
                }
                sets.add(goalSet)
            }

            if (showTrend && entries.size >= 2) {
                val trendEntries = calcMeasurementTrend(entries)
                val trendSet = LineDataSet(trendEntries, "Тренд").apply {
                    mode = LineDataSet.Mode.LINEAR
                    color = Color.Gray.toArgb()
                    lineWidth = 2f
                    setDrawCircles(false)
                    setDrawValues(false)
                }
                sets.add(trendSet)
            }

            chart.data = LineData(sets)
            val yValues = entries.map { it.y } + listOfNotNull(goal?.toFloat())
            chart.axisLeft.axisMinimum = (yValues.minOrNull() ?: 0f) - 5f
            chart.axisLeft.axisMaximum = (yValues.maxOrNull() ?: 0f) + 5f

            chart.invalidate()
        }
    )
}
