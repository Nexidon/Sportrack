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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.AppDatabase
import com.example.sportrack.data.model.DayAssignment
import com.example.sportrack.ui.components.SportButton
import com.example.sportrack.ui.components.SportCard
import com.example.sportrack.ui.components.SportTextField
import com.example.sportrack.ui.theme.SportRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    val defaultGroups = listOf(
        "Грудні м'язи", "Спина", "Ноги", "Плечі", "Біцепс",
        "Трицепс", "Прес", "Сідниці", "Ікри", "Передпліччя"
    )

    val muscleGroups = remember { mutableStateListOf(*defaultGroups.toTypedArray()) }
    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    val assignments = remember { mutableStateMapOf<String, List<String>>() }

    val editingDay = remember { mutableStateOf<String?>(null) }
    val isEditMode = remember { mutableStateOf(false) }
    val tempSelected = remember { mutableStateListOf<String>() }

    var showAddGroupDialog by remember { mutableStateOf(false) }
    var groupToDelete by remember { mutableStateOf<String?>(null) }
    var newGroupName by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val dao = db.dayAssignmentDao()

    LaunchedEffect(Unit) {
        val saved = dao.getAll()
        assignments.clear()
        saved.forEach { a ->
            val groupsForDay = a.groups.split(",").map { it.trim() }.filter { it.isNotBlank() }
            assignments[a.day] = groupsForDay
            groupsForDay.forEach { group ->
                if (!muscleGroups.contains(group)) muscleGroups.add(group)
            }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface)
    )

    // --- ДІАЛОГ: НОВА ГРУПА ---
    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text("Створити групу") },
            text = {
                SportTextField(value = newGroupName, onValueChange = { newGroupName = it }, placeholder = "Наприклад: Йога", modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                SportButton(text = "Додати", onClick = {
                    if (newGroupName.isNotBlank()) {
                        val trimmed = newGroupName.trim()
                        if (!muscleGroups.contains(trimmed)) muscleGroups.add(trimmed)
                        showAddGroupDialog = false
                        newGroupName = ""
                    }
                }, modifier = Modifier)
            },
            dismissButton = { SportButton(text = "Скасувати", color = Color.Gray, onClick = { showAddGroupDialog = false }, modifier = Modifier) }
        )
    }

    // --- ДІАЛОГ: ПІДТВЕРДЖЕННЯ ВИДАЛЕННЯ ГРУПИ ---
    if (groupToDelete != null) {
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text("Видалити групу?") },
            text = { Text("Група '${groupToDelete}' буде видалена з усіх днів розкладу. Вправи в базі залишаться.") },
            confirmButton = {
                SportButton(text = "Видалити", color = SportRed, modifier = Modifier, onClick = {
                    val groupName = groupToDelete!!
                    scope.launch {
                        // 1. Видаляємо зі списку доступних
                        muscleGroups.remove(groupName)
                        // 2. Очищаємо всі дні в базі від цієї групи
                        val allFromDb = dao.getAll()
                        allFromDb.forEach { entry ->
                            val updatedGroups = entry.groups.split(",")
                                .map { it.trim() }
                                .filter { it != groupName && it.isNotBlank() }

                            dao.insert(entry.copy(groups = updatedGroups.joinToString(",")))
                        }
                        // 3. Оновлюємо локальний стан розкладу
                        assignments.keys.forEach { day ->
                            assignments[day] = assignments[day]?.filter { it != groupName } ?: emptyList()
                        }
                        if (tempSelected.contains(groupName)) tempSelected.remove(groupName)
                        groupToDelete = null
                    }
                })
            },
            dismissButton = { SportButton(text = "Скасувати", onClick = { groupToDelete = null }, modifier = Modifier) }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Розклад", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { isEditMode.value = !isEditMode.value }) {
                        Icon(Icons.Default.Edit, null, tint = if (isEditMode.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(innerPadding).padding(12.dp)) {
            // Блок днів
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    days.forEach { day ->
                        val isSelected = editingDay.value == day
                        Card(
                            modifier = Modifier.weight(1f).height(64.dp).clickable {
                                editingDay.value = day
                                tempSelected.clear()
                                tempSelected.addAll(assignments[day] ?: emptyList())
                                isEditMode.value = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(day, color = if (isSelected) Color.White else Color.Unspecified)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showAddGroupDialog = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Створити нову групу")
            }

            Spacer(Modifier.height(16.dp))

            // Сітка груп
            LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(muscleGroups) { group ->
                    val isEditing = isEditMode.value && editingDay.value != null
                    val isActive = if (isEditing) tempSelected.contains(group) else assignments[editingDay.value]?.contains(group) == true

                    Box(modifier = Modifier.fillMaxSize()) {
                        SportCard(
                            title = group,
                            isActive = isActive,
                            onClick = {
                                if (isEditing) {
                                    if (tempSelected.contains(group)) tempSelected.remove(group) else tempSelected.add(group)
                                }
                            }
                        )

                        // Кнопка видалення (тільки для кастомних груп у режимі редагування)
                        if (isEditMode.value && !defaultGroups.contains(group)) {
                            IconButton(
                                onClick = { groupToDelete = group },
                                modifier = Modifier.align(Alignment.TopEnd).size(32.dp).padding(4.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = SportRed, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            if (editingDay.value != null) {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), elevation = CardDefaults.cardElevation(8.dp)) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!isEditMode.value) {
                            SportButton(text = "Редагувати ${editingDay.value}", modifier = Modifier.weight(1f), onClick = { isEditMode.value = true })
                        } else {
                            SportButton(text = "Зберегти", modifier = Modifier.weight(1f), onClick = {
                                val selected = tempSelected.toList()
                                assignments[editingDay.value!!] = selected
                                scope.launch {
                                    dao.insert(DayAssignment(editingDay.value!!, selected.joinToString(",")))
                                    isEditMode.value = false
                                }
                            })
                            OutlinedButton(onClick = { isEditMode.value = false }, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Close, null) }
                        }
                    }
                }
            }
        }
    }
}