package com.example.sportrack.ui.progress.sections

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportrack.ui.components.SportButton
import com.example.sportrack.ui.components.SportCheckbox
import com.example.sportrack.ui.components.SportCheckboxBlock
import com.example.sportrack.ui.components.SportProgressBar
import com.example.sportrack.ui.components.SportTextField
import com.example.sportrack.ui.progress.charts.WeightChart
import com.example.sportrack.ui.progress.components.WeightEntryItem
import com.example.sportrack.ui.progress.ProgressViewModel
import com.example.sportrack.ui.theme.SportGreen

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

    // 1. Находим текущий вес (последняя запись по дате)
    val currentEntry = entries.maxByOrNull { it.date }
    val currentWeight = currentEntry?.weight ?: 0.0

// 2. Находим стартовый вес (самая старая запись)
    val startEntry = entries.minByOrNull { it.date }
    val startWeight = startEntry?.weight ?: currentWeight

// 3. Считаем прогресс (Математика)
    val targetGoal = goal

// 2. Используем targetGoal вместо goal
    val progress = if (targetGoal != null && startWeight != targetGoal) {
        val totalToLose = startWeight - targetGoal // Теперь Kotlin видит, что это Double
        val alreadyLost = startWeight - currentWeight

        (alreadyLost / totalToLose).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ПЕРВЫЙ БЛОК: Ввод, График, Кнопки
        item {
            SportTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                placeholder = "Вага (кг)",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SportButton(
                text = "Зберегти вагу",
                onClick = {
                    weightInput.toDoubleOrNull()?.let { value ->
                        viewModel.saveWeight(value)
                        weightInput = ""
                        Toast.makeText(context, "Вага збережена!", Toast.LENGTH_SHORT).show()
                    } ?: Toast.makeText(context, "Введіть коректну вагу", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ... (Кнопка "Зберегти вагу" была выше)

            Spacer(modifier = Modifier.height(16.dp))

            // === НАЧАЛО НОВОГО БЛОКА ЦЕЛИ ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Верхняя строка: Заголовок + Карандаш
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "МОЯ ЦІЛЬ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${goal ?: "-"} кг",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Кнопка редактирования цели
                        IconButton(
                            onClick = { showGoalDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF1CB0F6), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Прогресс бар
                    SportProgressBar(
                        progress = progress,
                        modifier = Modifier.height(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Подписи снизу (Старт -> Финиш)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Старт: ${startWeight.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        // Сюда можно добавить % выполнения, если хочешь
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            // === КОНЕЦ НОВОГО БЛОКА ===

            Spacer(modifier = Modifier.height(16.dp))

            // ... (Дальше идут чекбокс и график)

            Row(verticalAlignment = Alignment.CenterVertically) {
                SportCheckboxBlock(checked = showTrend, onCheckedChange = { showTrend = it }, modifier = Modifier, text = "Показати трендову лінію")

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
        } // <--- Закрывает первый item

        // ВТОРОЙ БЛОК: Список истории (теперь он ВНУТРИ LazyColumn)
        item {
            AnimatedVisibility(
                visible = !historyCollapsed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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