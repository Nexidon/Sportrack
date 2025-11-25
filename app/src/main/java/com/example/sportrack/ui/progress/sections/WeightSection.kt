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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportrack.ui.progress.charts.WeightChart
import com.example.sportrack.ui.progress.components.WeightEntryItem
import com.example.sportrack.ui.progress.ProgressViewModel


@Composable
fun WeightSection(viewModel: ProgressViewModel) {
    val context = LocalContext.current
    var weightInput by remember { mutableStateOf("") }
    var showTrend by remember { mutableStateOf(false) }
    var historyCollapsed by remember { mutableStateOf(false) }
    var sortDescending by remember { mutableStateOf(true) }
    var showGoalDialog by remember { mutableStateOf(false) }

    val entries by viewModel.allEntries.collectAsState(initial = emptyList())
    val goal by viewModel.goal.collectAsState()

    val sortedEntries = if (sortDescending) entries.sortedByDescending { it.date }
    else entries.sortedBy { it.date }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Вага (кг)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    weightInput.toDoubleOrNull()?.let { value ->
                        viewModel.saveWeight(value)
                        weightInput = ""
                        Toast.makeText(context, "Вага збережена!", Toast.LENGTH_SHORT).show()
                    } ?: Toast.makeText(context, "Введіть коректну вагу", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Зберегти вагу") }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = goal?.let { "Поточна ціль: ${it} кг" } ?: "Ціль не встановлена",
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = { showGoalDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Змінити ціль")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showTrend, onCheckedChange = { showTrend = it })
                Text("Показати трендову лінію")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Графік зміни ваги:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            WeightChart(entries, goal, showTrend)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Історія ваги:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { historyCollapsed = !historyCollapsed }) {
                    Text(if (historyCollapsed) "Показати історію" else "Сховати історію")
                }
                Button(onClick = { sortDescending = !sortDescending }) {
                    Text(if (sortDescending) "Спочатку нові" else "Спочатку старі")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            AnimatedVisibility(
                visible = !historyCollapsed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    sortedEntries.forEach { entry ->
                        WeightEntryItem(
                            entry = entry,
                            onDelete = { viewModel.deleteWeight(it) },
                            onUpdate = { updatedEntry -> viewModel.updateWeight(updatedEntry) }
                        )
                    }
                }
            }
        }
    }
}
