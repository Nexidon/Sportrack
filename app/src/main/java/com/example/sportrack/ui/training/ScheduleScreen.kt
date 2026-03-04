package com.example.sportrack.ui.training

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.AppDatabase
import com.example.sportrack.data.model.DayAssignment
import com.example.sportrack.ui.components.SportCard // Убедись, что SportCard создан
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    val muscleGroups = listOf(
        "Грудні м'язи", "Спина", "Ноги", "Плечі", "Біцепс",
        "Трицепс", "Прес", "Ягодиці", "Ікри", "Передпліччя"
    )

    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    val assignments = remember { mutableStateMapOf<String, List<String>>() }

    // Состояние редактирования
    val editingDay = remember { mutableStateOf<String?>(null) }
    val isEditMode = remember { mutableStateOf(false) }
    val tempSelected = remember { mutableStateListOf<String>() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).dayAssignmentDao() }

    // Загрузка данных
    LaunchedEffect(Unit) {
        val saved = dao.getAll()
        assignments.clear()
        saved.forEach { a ->
            assignments[a.day] = a.groups.split(",").filter { it.isNotBlank() }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Rozklad", style = MaterialTheme.typography.titleLarge) // Или "Розклад тренувань"
                        Text(
                            text = "Натисни на день",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { isEditMode.value = !isEditMode.value }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = if (isEditMode.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // --- БЛОК ДНЕЙ НЕДЕЛИ ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp), // Более округлые углы
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp), // Чуть плотнее
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        days.forEach { day ->
                            val assignedForDay = assignments[day] ?: emptyList()
                            val hasAssigned = assignedForDay.isNotEmpty()
                            val isSelectedDay = editingDay.value == day

                            // Тут используем обычную Card, так как это маленькие кнопки
                            // Но стилизуем их под наш дизайн
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(64.dp)
                                    .clickable {
                                        if (isSelectedDay) {
                                            editingDay.value = null
                                            tempSelected.clear()
                                            isEditMode.value = false
                                        } else {
                                            editingDay.value = day
                                            tempSelected.clear()
                                            tempSelected.addAll(assignedForDay)
                                            isEditMode.value = false
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                border = if (hasAssigned && !isSelectedDay) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelectedDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isSelectedDay) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (hasAssigned) {
                                            // Маленькая точка вместо текста, чтобы не забивать место
                                            Text("•", color = if(isSelectedDay) Color.White else MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // --- СЕТКА МЫШЦ (SportCard) ---
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(muscleGroups) { group ->
                        val isEditing = isEditMode.value && editingDay.value != null
                        val inTemp = tempSelected.contains(group)
                        val assignedDays = assignments.filter { it.value.contains(group) }.keys.toList()

                        // Логика: активна ли карточка?
                        // Если редактируем -> активна, если выбрана во временном списке.
                        // Если просто смотрим -> активна, если выбрана в текущем дне (editingDay)
                        val highlightForSelectedDay = editingDay.value != null && assignments[editingDay.value]?.contains(group) == true
                        val isActive = if (isEditing) inTemp else highlightForSelectedDay

                        // Логика: какой подтекст показать?
                        val subtitleText = when {
                            isEditing && inTemp -> "✓ Вибрано"
                            !isEditing && assignedDays.isNotEmpty() -> assignedDays.joinToString(", ")
                            else -> null
                        }

                        // ВОТ ЗДЕСЬ ИСПОЛЬЗУЕМ НОВЫЙ КОМПОНЕНТ
                        SportCard(
                            title = group,
                            subtitle = subtitleText,
                            isActive = isActive,
                            onClick = {
                                if (isEditing) {
                                    if (inTemp) tempSelected.remove(group)
                                    else tempSelected.add(group)
                                }
                            }
                        )
                    }
                }

                // --- ПАНЕЛЬ РЕДАКТИРОВАНИЯ (Снизу) ---
                if (editingDay.value != null) {
                    Spacer(Modifier.height(12.dp))
                    val day = editingDay.value!!

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Редагування: $day", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))

                            if (!isEditMode.value) {
                                Button(
                                    onClick = { isEditMode.value = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(Modifier.size(8.dp))
                                    Text("Змінити групи")
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Кнопка Сохранить
                                    Button(
                                        onClick = {
                                            val selected = tempSelected.toList()
                                            assignments[day] = selected
                                            scope.launch {
                                                dao.insert(DayAssignment(day, selected.joinToString(",")))
                                                snackbarHostState.showSnackbar("Збережено для $day")
                                            }
                                            editingDay.value = null
                                            tempSelected.clear()
                                            isEditMode.value = false
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Зберегти")
                                    }

                                    // Кнопка Отмена (Outlined)
                                    OutlinedButton(
                                        onClick = {
                                            editingDay.value = null
                                            tempSelected.clear()
                                            isEditMode.value = false
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}