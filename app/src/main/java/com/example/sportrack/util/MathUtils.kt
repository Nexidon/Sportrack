package com.example.sportrack.util

import com.example.sportrack.data.model.WeightEntry
import com.github.mikephil.charting.data.Entry


    fun calcMeasurementTrend(entries: List<Entry>): List<Entry> {
        if (entries.size < 2) return emptyList()

        val xs = entries.map { it.x.toDouble() }
        val ys = entries.map { it.y.toDouble() }

        val xMean = xs.average()
        val yMean = ys.average()

        var numerator = 0.0
        var denominator = 0.0
        for (i in xs.indices) {
            numerator += (xs[i] - xMean) * (ys[i] - yMean)
            denominator += (xs[i] - xMean) * (xs[i] - xMean)
        }

        val slope = numerator / denominator
        val intercept = yMean - slope * xMean

        return xs.map { x -> Entry(x.toFloat(), (slope * x + intercept).toFloat()) }
    }


    fun calcTrend(entries: List<WeightEntry>): List<Entry> {
        if (entries.size < 2) return emptyList()

        val xs = entries.indices.map { it.toDouble() }
        val ys = entries.map { it.weight.toDouble() }

        val xMean = xs.average()
        val yMean = ys.average()

        var numerator = 0.0
        var denominator = 0.0
        for (i in xs.indices) {
            numerator += (xs[i] - xMean) * (ys[i] - yMean)
            denominator += (xs[i] - xMean) * (xs[i] - xMean)
        }

        val slope = numerator / denominator
        val intercept = yMean - slope * xMean

        return xs.map { x -> Entry(x.toFloat(), (slope * x + intercept).toFloat()) }
    }
