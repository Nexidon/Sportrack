package com.example.sportrack.ui.progress.sections

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.model.Period
import com.example.sportrack.data.model.StrengthEntry
import com.example.sportrack.ui.progress.ChartType
import com.example.sportrack.ui.progress.SegmentedButtons
import com.example.sportrack.ui.progress.charts.StrengthBarProgress
import com.example.sportrack.ui.progress.charts.StrengthLineProgress
import com.example.sportrack.ui.progress.components.StrengthEntryItem
import com.example.sportrack.ui.progress.ProgressViewModel

@Composable
fun StrengthsSection(viewModel: ProgressViewModel) {
    val context = LocalContext.current
    val strengths by viewModel.allStrengths.collectAsState(initial = emptyList())

    var liftType by remember { mutableStateOf("Усі типи") }
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }
    var newLiftType by remember { mutableStateOf("") }

    var period by remember { mutableStateOf(Period.ALL) }
    var historyCollapsed by remember { mutableStateOf(false) }

    // Елементи керування переглядом прогресу
    var selectedChart by remember { mutableStateOf(ChartType.LINE) }
    var showTrend by remember { mutableStateOf(true) }
    var showGoal by remember { mutableStateOf(true) }

    // Список типів підйомів на основі даних
    val liftTypes = remember(strengths) {
        (strengths.map { it.liftType }.distinct().sorted()).let { list ->
            list
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // -- Введення нового запису --
        item {
            Text("Новий силовий запис", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newLiftType,
                    onValueChange = { newLiftType = it },
                    label = { Text("Тип підйому") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Вага (кг)") },
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = repsInput,
                    onValueChange = { repsInput = it },
                    label = { Text("Повт.") },
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val w = weightInput.toDoubleOrNull()
                val r = repsInput.toIntOrNull()
                if (newLiftType.isBlank() || w == null || r == null) {
                    Toast.makeText(context, "Заповніть усі поля коректно", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val entry = StrengthEntry(
                    date = System.currentTimeMillis(),
                    liftType = newLiftType.trim(),
                    weight = w,
                    reps = r
                )
                viewModel.saveStrength(entry)
                newLiftType = ""
                weightInput = ""
                repsInput = ""
                Toast.makeText(context, "Запис збережено", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Зберегти запис")
            }
        }

        // -- Налаштування фільтра/перегляду --
        item {
            Spacer(Modifier.height(6.dp))
            Text("Перегляд прогресу", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            // Dropdown вибору типу підйому (або "Усі типи")
            var expanded by remember { mutableStateOf(false) }
            val options = listOf("Усі типи") + liftTypes
            Box {
                OutlinedTextField(
                    value = liftType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тип підйому") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Default.Edit, contentDescription = "Обрати")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { opt ->
                        DropdownMenuItem(text = { Text(opt, maxLines = 1, overflow = TextOverflow.Ellipsis) }, onClick = {
                            liftType = opt
                            expanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Період
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Period.values().forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { period = p },
                        label = { Text(p.name.lowercase().replaceFirstChar { it.uppercaseChar() }) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Адаптивний блок: SegmentedButtons + чекбокси (перенос на вузьких екранах)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 420.dp) {
                    // вузький екран — вертикальна компоновка
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SegmentedButtons(selectedChart) { selectedChart = it }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = showTrend, onCheckedChange = { showTrend = it })
                                Text("Показати тренд", modifier = Modifier.padding(start = 4.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = showGoal, onCheckedChange = { showGoal = it })
                                Text("Показати ціль", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                } else {
                    // широкий екран — все в один ряд, чекбокси вирівняні праворуч
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.wrapContentWidth()) {
                            SegmentedButtons(selectedChart) { selectedChart = it }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                            Checkbox(checked = showTrend, onCheckedChange = { showTrend = it })
                            Text("Показати тренд", modifier = Modifier.padding(start = 4.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                            Checkbox(checked = showGoal, onCheckedChange = { showGoal = it })
                            Text("Показати ціль", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // -- Графік прогресу --
        item {
            val filtered = remember(strengths, period) {
                viewModel.filterStrengthsForPeriod(strengths, period)
            }

            // далі фільтруємо по liftType якщо обраний конкретний тип
            val filteredByType = remember(filtered, liftType) {
                if (liftType == "Усі типи") filtered else filtered.filter { it.liftType == liftType }
            }

            when (selectedChart) {
                ChartType.LINE -> {
                    StrengthLineProgress(
                        entries = filteredByType,
                        liftType = if (liftType == "Усі типи") null else liftType,
                        showTrend = showTrend,
                        showGoal = showGoal,
                        viewModel = viewModel
                    )
                }
                ChartType.BAR -> {
                    StrengthBarProgress(
                        entries = filteredByType,
                        viewModel = viewModel
                    )
                }
            }
        }

        // -- Історія записів --
        item {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Історія записів:", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { historyCollapsed = !historyCollapsed }) {
                    Text(if (historyCollapsed) "Показати" else "Сховати")
                }
            }
        }

        if (!historyCollapsed) {
            items(strengths) { entry ->
                StrengthEntryItem(
                    entry = entry,
                    onDelete = { viewModel.deleteStrength(it) },
                    onUpdate = { viewModel.updateStrength(it) }
                )
            }
        }
    }
}
