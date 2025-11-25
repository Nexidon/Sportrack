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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.AppDatabase
import com.example.sportrack.data.model.DayAssignment
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    val muscleGroups = listOf(
        "Грудні м'язи",
        "Спина",
        "Ноги",
        "Плечі",
        "Біцепс",
        "Трицепс",
        "Прес",
        "Ягодиці",
        "Ікри",
        "Передпліччя"
    )

    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    val assignments = remember { mutableStateMapOf<String, List<String>>() }
    val editingDay = remember { mutableStateOf<String?>(null) }
    val isEditMode = remember { mutableStateOf(false) }
    val tempSelected = remember { mutableStateListOf<String>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).dayAssignmentDao() }

    // Завантаження з БД
    LaunchedEffect(Unit) {
        val saved = dao.getAll()
        assignments.clear()
        saved.forEach { a ->
            assignments[a.day] = a.groups.split(",").filter { it.isNotBlank() }
        }
    }

    // Фон — лёгкий вертикальный градиент (только визуально)
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
                    Column {
                        Text("Розклад тренувань", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Натисни на день для перегляду або редагування",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Внешняя иконка для переключения режима редактирования всего экрана
                    IconButton(onClick = { isEditMode.value = !isEditMode.value }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Тoggle edit mode",
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Рядок днів — поміщений у картку для акценту
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            days.forEach { day ->
                                val assignedForDay = assignments[day] ?: emptyList()
                                val hasAssigned = assignedForDay.isNotEmpty()
                                val isSelectedDay = editingDay.value == day

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(72.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            if (isSelectedDay) {
                                                editingDay.value = null
                                                tempSelected.clear()
                                                isEditMode.value = false
                                            } else {
                                                editingDay.value = day
                                                tempSelected.clear()
                                                tempSelected.addAll(assignedForDay)
                                                isEditMode.value = false // Клік — перегляд
                                            }
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    border = if (hasAssigned) BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    ) else null,
                                    colors = if (isSelectedDay) CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ) else CardDefaults.cardColors(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelectedDay) 8.dp else 4.dp)
                                ) {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                day,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (isSelectedDay) MaterialTheme.colorScheme.onSecondaryContainer
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (hasAssigned) {
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    "${assignedForDay.size} груп",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Грід груп (з підсвічуваннями) — даємо відступи і поміщаємо в box з паддінгом
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(muscleGroups) { group ->
                            val isEditing = isEditMode.value && editingDay.value != null
                            val inTemp = tempSelected.contains(group)
                            val assignedDays = assignments.filter { it.value.contains(group) }.keys.toList()
                            val highlightForSelectedDay =
                                editingDay.value != null && assignments[editingDay.value]?.contains(group) == true

                            Card(
                                modifier = Modifier
                                    .height(110.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isEditing) {
                                            if (inTemp) tempSelected.remove(group)
                                            else tempSelected.add(group)
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                border = when {
                                    isEditing && inTemp -> BorderStroke(
                                        2.dp,
                                        MaterialTheme.colorScheme.primary
                                    )
                                    assignedDays.isNotEmpty() -> BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                    else -> null
                                },
                                colors = if (highlightForSelectedDay && !isEditing) {
                                    CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                } else {
                                    CardDefaults.cardColors()
                                },
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            group,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )

                                        if (isEditing && inTemp) {
                                            Spacer(Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = "Вибрано",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(Modifier.size(6.dp))
                                                Text(
                                                    "Вибрано",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        if (!isEditing && assignedDays.isNotEmpty()) {
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                "Призначено: ${assignedDays.joinToString(", ")}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Кнопки управління — внизу, у карточці для візуальної стабільності
                    if (editingDay.value != null) {
                        Spacer(Modifier.height(12.dp))
                        val day = editingDay.value!!

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                if (!isEditMode.value) {
                                    Button(
                                        onClick = { isEditMode.value = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Редагувати")
                                        Spacer(Modifier.size(8.dp))
                                        Text("Редагувати")
                                    }
                                } else {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
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
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Зберегти")
                                            Spacer(Modifier.size(6.dp))
                                            Text("Зберегти")
                                        }

                                        TextButton(
                                            onClick = {
                                                tempSelected.clear()
                                                tempSelected.addAll(assignments[day] ?: emptyList())
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Скасовано")
                                                }
                                            },
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Скасувати")
                                            Spacer(Modifier.size(6.dp))
                                            Text("Скасувати")
                                        }

                                        TextButton(
                                            onClick = {
                                                editingDay.value = null
                                                tempSelected.clear()
                                                isEditMode.value = false
                                            },
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Закрити")
                                            Spacer(Modifier.size(6.dp))
                                            Text("Закрити")
                                        }
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
