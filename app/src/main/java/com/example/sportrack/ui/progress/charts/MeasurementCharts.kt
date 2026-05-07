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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sportrack.data.model.MeasurementEntry
import com.example.sportrack.util.calcMeasurementTrend
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeasurementChart(
    measurements: List<MeasurementEntry>,
    valueSelector: (MeasurementEntry) -> Double?,
    label: String,
    lineColor: Color,
    showTrend: Boolean = false,
    goal: Double? = null
) {
    // Відфільтровуємо порожні значення та сортуємо за датою (від старих до нових)
    val validMeasurements = remember(measurements) {
        measurements.sortedBy { it.date }.filter { valueSelector(it) != null }
    }

    if (validMeasurements.isEmpty()) return

    val chartDataPoints = remember(validMeasurements) {
        validMeasurements.mapIndexed { index, entry ->
            Entry(index.toFloat(), valueSelector(entry)!!.toFloat())
        }
    }

    val dateLabels = remember(validMeasurements) {
        val format = SimpleDateFormat("dd.MM", Locale.getDefault())
        validMeasurements.map { format.format(Date(it.date)) }
    }

    val mainColor = lineColor.toArgb()
    val axisColor = AndroidColor.parseColor("#999999")

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
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        legend.isEnabled = false
                        setBackgroundColor(AndroidColor.TRANSPARENT)
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(false)
                        setDrawGridBackground(false)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            textColor = axisColor
                            setDrawGridLines(false)
                            setDrawAxisLine(false)
                            granularity = 1f
                            valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    val index = value.toInt()
                                    return if (index in dateLabels.indices) dateLabels[index] else ""
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
                    val dataSet = LineDataSet(chartDataPoints, label).apply {
                        color = mainColor
                        lineWidth = 3f
                        setDrawCircles(true)
                        setCircleColor(AndroidColor.WHITE)
                        circleRadius = 5f
                        circleHoleRadius = 3f
                        circleHoleColor = mainColor
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        setDrawFilled(true)
                        fillColor = mainColor
                        fillAlpha = 50
                        setDrawValues(false)
                    }

                    val sets = mutableListOf<ILineDataSet>(dataSet)

                    if (showTrend && chartDataPoints.size >= 2) {
                        val trendPoints = calcMeasurementTrend(chartDataPoints)
                        val trendSet = LineDataSet(trendPoints, "Тренд").apply {
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
                    if (goal != null) {
                        val limitLine = LimitLine(goal.toFloat(), "Ціль").apply {
                            this.lineColor = AndroidColor.parseColor("#FFC107")
                            lineWidth = 2f
                            enableDashedLine(10f, 10f, 0f)
                            textColor = axisColor
                            textSize = 10f
                        }
                        chart.axisLeft.addLimitLine(limitLine)
                    }

                    chart.data = LineData(sets)
                    chart.invalidate()
                }
            )
        }
    }
}