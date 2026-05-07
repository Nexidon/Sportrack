package com.example.sportrack.ui.progress.sections

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.model.MeasurementEntry
import com.example.sportrack.ui.components.SportButton
import com.example.sportrack.ui.components.SportCheckboxBlock
import com.example.sportrack.ui.components.SportTextField
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
    var historyCollapsed by remember { mutableStateOf(false) }
    var sortDescending by remember { mutableStateOf(true) }

    val sortedMeasurements = if (sortDescending) measurements.sortedByDescending { it.date }
    else measurements.sortedBy { it.date }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Новий замір:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            SportTextField(
                value = chest,
                onValueChange = { chest = it },
                placeholder = "Груди (см)",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(Modifier.height(8.dp))
            SportTextField(
                value = waist,
                onValueChange = { waist = it },
                placeholder = "Талія (см)",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(Modifier.height(8.dp))
            SportTextField(
                value = hips,
                onValueChange = { hips = it },
                placeholder = "Стегна (см)",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(Modifier.height(8.dp))
            SportTextField(
                value = biceps,
                onValueChange = { biceps = it },
                placeholder = "Біцепс (см)",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(16.dp))

            SportButton(
                text = "Зберегти замір",
                onClick = {
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
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SportCheckboxBlock(
                    checked = showTrend,
                    onCheckedChange = { showTrend = it },
                    modifier = Modifier,
                    text = "Показати трендову лінію"
                )
            }

            Spacer(Modifier.height(16.dp))

            Text("Графіки замірів:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            MeasurementChart(measurements, { it.chest }, "Груди", Color.Cyan, showTrend = showTrend)
            MeasurementChart(measurements, { it.waist }, "Талія", Color.Green, showTrend = showTrend)
            MeasurementChart(measurements, { it.hips }, "Стегна", Color.Magenta, showTrend = showTrend)
            MeasurementChart(measurements, { it.biceps }, "Біцепс", Color.Yellow, showTrend = showTrend)

            Spacer(Modifier.height(16.dp))

            Text("Історія замірів:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SportButton(
                    onClick = { historyCollapsed = !historyCollapsed },
                    text = if (historyCollapsed) "Показати" else "Сховати",
                    color = com.example.sportrack.ui.theme.SportBlue,
                    modifier = Modifier.weight(1f)
                )
                SportButton(
                    onClick = { sortDescending = !sortDescending },
                    text = if (sortDescending) "Спочатку нові" else "Спочатку старі",
                    color = com.example.sportrack.ui.theme.SportBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            AnimatedVisibility(
                visible = !historyCollapsed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    sortedMeasurements.forEach { entry ->
                        MeasurementEntryItem(
                            entry = entry,
                            onDelete = { viewModel.deleteMeasurement(it) },
                            onUpdate = { updatedEntry -> viewModel.updateMeasurement(updatedEntry) }
                        )
                    }
                }
            }
        }
    }
}