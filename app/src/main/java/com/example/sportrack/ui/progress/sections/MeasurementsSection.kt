package com.example.sportrack.ui.progress.sections

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.model.MeasurementEntry
import com.example.sportrack.ui.progress.charts.MeasurementChart
import com.example.sportrack.ui.progress.components.MeasurementEntryItem
import com.example.sportrack.ui.progress.ProgressViewModel

@Composable
fun MeasurementsSection(viewModel: ProgressViewModel) {
    val context = LocalContext.current
    val measurements: List<MeasurementEntry> by viewModel.allMeasurements.collectAsState(initial = emptyList())

    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }
    var biceps by remember { mutableStateOf("") }

    var showTrend by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = showTrend, onCheckedChange = { showTrend = it })
        Text("Показати трендову лінію")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Новий замір:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = chest, onValueChange = { chest = it }, label = { Text("Груди (см)") })
            OutlinedTextField(value = waist, onValueChange = { waist = it }, label = { Text("Талія (см)") })
            OutlinedTextField(value = hips, onValueChange = { hips = it }, label = { Text("Стегна (см)") })
            OutlinedTextField(value = biceps, onValueChange = { biceps = it }, label = { Text("Біцепс (см)") })

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                val entry = MeasurementEntry(
                    date = System.currentTimeMillis(),
                    chest = chest.toDoubleOrNull(),
                    waist = waist.toDoubleOrNull(),
                    hips = hips.toDoubleOrNull(),
                    biceps = biceps.toDoubleOrNull()
                )
                viewModel.saveMeasurement(entry)
                chest = ""
                waist = ""
                hips = ""
                biceps = ""
                Toast.makeText(context, "Замір збережено!", Toast.LENGTH_SHORT).show()
            }) {
                Text("Зберегти замір")
            }
        }

        item {
            Text("Графіки замірів:", style = MaterialTheme.typography.titleMedium)
            MeasurementChart(measurements, { it.chest }, "Груди", Color.Cyan, showTrend = showTrend)
            MeasurementChart(measurements, { it.waist }, "Талія", Color.Green, showTrend = showTrend)
            MeasurementChart(measurements, { it.hips }, "Стегна", Color.Magenta, showTrend = showTrend)
            MeasurementChart(measurements, { it.biceps }, "Біцепс", Color.Yellow, showTrend = showTrend)
        }

        item {
            Text("Історія замірів:", style = MaterialTheme.typography.titleMedium)
        }

        items(measurements) { entry ->
            MeasurementEntryItem(
                entry,
                onDelete = { viewModel.deleteMeasurement(it) },
                onUpdate = { viewModel.updateMeasurement(it) }
            )
        }
    }
}

