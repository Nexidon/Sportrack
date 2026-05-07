package com.example.sportrack.ui.progress.sections

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.model.Period
import com.example.sportrack.data.model.StrengthEntry
import com.example.sportrack.ui.components.SportButton
import com.example.sportrack.ui.components.SportCheckboxBlock
import com.example.sportrack.ui.components.SportTextField
import com.example.sportrack.ui.progress.ChartType
import com.example.sportrack.ui.progress.charts.StrengthBarProgress
import com.example.sportrack.ui.progress.charts.StrengthLineProgress
import com.example.sportrack.ui.progress.components.StrengthEntryItem
import com.example.sportrack.ui.progress.ProgressViewModel

@Composable
fun StrengthsSection(viewModel: ProgressViewModel) {
    val context = LocalContext.current
    val strengths by viewModel.allStrengths.collectAsState(initial = emptyList())

    var liftType by remember { mutableStateOf("Усі вправи") }
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }
    var newLiftType by remember { mutableStateOf("") }

    var period by remember { mutableStateOf(Period.ALL) }
    var historyCollapsed by remember { mutableStateOf(false) }

    var selectedChart by remember { mutableStateOf(ChartType.LINE) }
    var showTrend by remember { mutableStateOf(true) }
    var showGoal by remember { mutableStateOf(true) }

    val liftTypes = remember(strengths) {
        strengths.map { it.liftType }.distinct().sorted()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Новий силовий запис", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SportTextField(
                    value = newLiftType,
                    onValueChange = { newLiftType = it },
                    placeholder = "Тип вправи",
                    modifier = Modifier.weight(1.2f)
                )
                SportTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    placeholder = "Вага (кг)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                SportTextField(
                    value = repsInput,
                    onValueChange = { repsInput = it },
                    placeholder = "Повт.",
                    modifier = Modifier.weight(0.8f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(12.dp))

            SportButton(
                text = "Зберегти запис",
                onClick = {
                    val w = weightInput.toDoubleOrNull()
                    val r = repsInput.toIntOrNull()
                    if (newLiftType.isBlank() || w == null || r == null) {
                        Toast.makeText(context, "Заповніть усі поля коректно", Toast.LENGTH_SHORT).show()
                        return@SportButton
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
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("Перегляд прогресу", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }
            val options = listOf("Усі вправи") + liftTypes

            Box {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ОБРАНА ВПРАВА",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = liftType,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Обрати вправу",
                            tint = Color.Gray
                        )
                    }
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt, fontWeight = FontWeight.Medium) },
                            onClick = {
                                liftType = opt
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Кастомний перемикач для періоду (Замість FilterChip)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                border = BorderStroke(2.dp, Color(0xFFE5E5E5))
            ) {
                Row(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
                    val periods = listOf(Period.WEEK to "Тиждень", Period.MONTH to "Місяць", Period.YEAR to "Рік", Period.ALL to "Усі")
                    periods.forEach { (p, label) ->
                        val isSelected = period == p
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) Color.White else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { period = p }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Кастомний перемикач типу графіка (Замість SegmentedButtons)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                border = BorderStroke(2.dp, Color(0xFFE5E5E5))
            ) {
                Row(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
                    val charts = listOf(ChartType.LINE to "Графік лінії", ChartType.BAR to "Стовпці")
                    charts.forEach { (c, label) ->
                        val isSelected = selectedChart == c
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) Color.White else Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedChart = c }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SportCheckboxBlock(
                    checked = showTrend,
                    onCheckedChange = { showTrend = it },
                    text = "Тренд",
                    modifier = Modifier.weight(1f)
                )
                SportCheckboxBlock(
                    checked = showGoal,
                    onCheckedChange = { showGoal = it },
                    text = "Ціль",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))
        }

        item {
            val filtered = remember(strengths, period) {
                viewModel.filterStrengthsForPeriod(strengths, period)
            }

            val filteredByType = remember(filtered, liftType) {
                if (liftType == "Усі вправи") filtered else filtered.filter { it.liftType == liftType }
            }

            when (selectedChart) {
                ChartType.LINE -> {
                    StrengthLineProgress(
                        entries = filteredByType,
                        liftType = if (liftType == "Усі вправи") null else liftType,
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

        item {
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Історія записів:", style = MaterialTheme.typography.titleMedium)
                SportButton(
                    onClick = { historyCollapsed = !historyCollapsed },
                    text = if (historyCollapsed) "Показати" else "Сховати",
                    color = com.example.sportrack.ui.theme.SportBlue,
                    modifier = Modifier
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
                    strengths.forEach { entry ->
                        StrengthEntryItem(
                            entry = entry,
                            onDelete = { viewModel.deleteStrength(it) },
                            onUpdate = { viewModel.updateStrength(it) }
                        )
                    }
                }
            }
        }
    }
}