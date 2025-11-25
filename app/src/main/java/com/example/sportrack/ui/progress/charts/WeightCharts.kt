package com.example.sportrack.ui.progress.charts

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.sportrack.R
import com.example.sportrack.data.model.WeightEntry
import com.example.sportrack.util.calcTrend
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF

@Composable
fun WeightChart(entries: List<WeightEntry>, goal: Double?, showTrend: Boolean) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = "" }
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisRight.isEnabled = false
                legend.isEnabled = false

                setBackgroundColor(Color.Black.toArgb())
                xAxis.textColor = Color.White.toArgb()
                axisLeft.textColor = Color.White.toArgb()
                legend.textColor = Color.White.toArgb()
                xAxis.gridColor = Color.Gray.toArgb()
                axisLeft.gridColor = Color.Gray.toArgb()

                marker = object : MarkerView(context, R.layout.marker_weight) {
                    private val tvContent: TextView = findViewById(R.id.tvContent)

                    override fun refreshContent(
                        e: Entry?,
                        highlight: Highlight?
                    ) {
                        tvContent.text = e?.y?.let { "$it кг" } ?: ""
                        super.refreshContent(e, highlight)
                    }

                    override fun getOffset(): MPPointF {
                        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
                    }
                }
            }
        },
        update = { chart ->
            val lineEntries = entries.mapIndexed { index, e -> Entry(index.toFloat(), e.weight.toFloat()) }

            val dataSet = LineDataSet(lineEntries, "Вага").apply {
                setDrawValues(false)
                setDrawCircles(true)
                lineWidth = 2f
                color = Color.Cyan.toArgb()
                setCircleColor(Color.Magenta.toArgb())
            }

            val dataSets = mutableListOf<LineDataSet>(dataSet)

            if (showTrend && entries.size >= 2) {
                val trendEntries = calcTrend(entries)
                val trendSet = LineDataSet(trendEntries, "Тренд").apply {
                    setDrawValues(false)
                    setDrawCircles(false)
                    lineWidth = 2f
                    color = Color.Red.toArgb()
                }
                dataSets.add(trendSet)
            }

            chart.data = LineData(dataSets.map { it as ILineDataSet })

            chart.axisLeft.removeAllLimitLines()

            goal?.let {
                val ll = LimitLine(it.toFloat(), "Ціль").apply {
                    lineWidth = 2f
                    lineColor = Color.Green.toArgb()
                    textColor = Color.White.toArgb()
                }
                chart.axisLeft.addLimitLine(ll)

                val currentMin = (entries.minOfOrNull { it.weight }?.toFloat() ?: 0f).coerceAtMost(it.toFloat())
                val currentMax = (entries.maxOfOrNull { it.weight }?.toFloat() ?: 0f).coerceAtLeast(it.toFloat())
                chart.axisLeft.axisMinimum = currentMin - 1f
                chart.axisLeft.axisMaximum = currentMax + 1f
            }

            chart.invalidate()
        }

    )
}

