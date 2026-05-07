package com.example.sportrack.ui.training

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
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
import com.example.sportrack.data.AppDatabase
import com.example.sportrack.data.model.Exercise
import com.example.sportrack.ui.components.SportButton
import com.example.sportrack.ui.components.SportCheckboxBlock
import com.example.sportrack.ui.components.SportTextField
import com.example.sportrack.ui.theme.SportRed
import kotlinx.coroutines.launch

// Функція-парсер (залишаємо без змін)
fun parseSetsAndReps(setsInput: String, repsInput: String): Pair<Int?, Int?> {
    val parts = setsInput.lowercase().split(Regex("[xх*\\- ]+"))
    return if (parts.size >= 2) {
        val s = parts[0].filter { it.isDigit() }.toIntOrNull()
        val r = parts[1].filter { it.isDigit() }.toIntOrNull()
        Pair(s, r)
    } else {
        val s = setsInput.filter { it.isDigit() }.toIntOrNull()
        val r = repsInput.filter { it.isDigit() }.toIntOrNull()
        Pair(s, r)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesLibraryScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val exerciseDao = remember { db.exerciseDao() }
    val dayAssignmentDao = remember { db.dayAssignmentDao() }

    // Використовуємо звичайні State<List>, щоб Compose бачив зміну посилання
    var exercises by remember { mutableStateOf(emptyList<Exercise>()) }
    var userGroups by remember { mutableStateOf(emptyList<String>()) }
    var selectedGroup by remember { mutableStateOf("Усі") }

    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // Функція оновлення, яка гарантовано змінює об'єкти списків
    fun refresh() {
        scope.launch {
            // 1. Отримуємо вправи
            exercises = exerciseDao.getAll()

            // 2. Отримуємо всі призначення днів, щоб витягнути назви груп
            val allAssignments = dayAssignmentDao.getAll()
            val groupsFromSchedule = allAssignments.flatMap { it.groups.split(",") }
                .map { it.trim() }
                .filter { it.isNotBlank() }

            userGroups = groupsFromSchedule.distinct()
        }
    }

    // Оновлюємо дані щоразу, коли цей екран з'являється в полі зору
    LaunchedEffect(Unit) {
        refresh()
    }

    // Тепер це точно перерахується, бо exercises та userGroups — це нові List
    val muscleGroups = remember(exercises, userGroups) {
        val base = listOf("Усі", "Грудні м'язи", "Спина", "Ноги", "Плечі", "Біцепс", "Трицепс", "Прес", "Сідниці", "Ікри", "Передпліччя")
        val fromExercises = exercises.map { it.primaryGroup }

        (base + fromExercises + userGroups)
            .distinct()
            .sortedBy { if (it == "Усі") "" else it }
    }

    val filteredExercises = remember(selectedGroup, exercises) {
        if (selectedGroup == "Усі") exercises
        else exercises.filter { it.primaryGroup == selectedGroup }
    }

    // --- ДІАЛОГИ (Додавання, Редагування, Видалення - без змін) ---
    // ... (залишаємо код діалогів з попередньої версії)
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var group by remember { mutableStateOf("") }
        var isTimed by remember { mutableStateOf(false) }
        var setsInput by remember { mutableStateOf("") }
        var repsInput by remember { mutableStateOf("") }
        var rest by remember { mutableStateOf("60") }
        var duration by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Додати вправу", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SportTextField(value = name, onValueChange = { name = it }, placeholder = "Назва", modifier = Modifier.fillMaxWidth())
                    Box {
                        OutlinedTextField(
                            value = group, onValueChange = { group = it }, label = { Text("Група") },
                            trailingIcon = { IconButton(onClick = { expanded = !expanded }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            muscleGroups.filter { it != "Усі" }.forEach { grp ->
                                DropdownMenuItem(text = { Text(grp) }, onClick = { group = grp; expanded = false })
                            }
                        }
                    }
                    SportCheckboxBlock(checked = isTimed, onCheckedChange = { isTimed = it }, text = "На час", modifier = Modifier)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isTimed) {
                            SportTextField(value = duration, onValueChange = { duration = it }, placeholder = "Час", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        } else {
                            SportTextField(value = setsInput, onValueChange = { setsInput = it }, placeholder = "Сети/3х12", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
                            SportTextField(value = repsInput, onValueChange = { repsInput = it }, placeholder = "Репи", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
                        }
                    }
                    SportTextField(value = rest, onValueChange = { rest = it }, placeholder = "Відпочинок (сек)", modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                SportButton(text = "Додати", modifier = Modifier, onClick = {
                    if (name.isNotBlank() && group.isNotBlank()) {
                        scope.launch {
                            val (fSets, fReps) = parseSetsAndReps(setsInput, repsInput)
                            exerciseDao.insert(Exercise(name = name.trim(), primaryGroup = group.trim(), isTimed = isTimed, defaultSets = fSets, defaultReps = fReps, defaultDurationSec = duration.toIntOrNull(), restSec = rest.toIntOrNull()))
                            showAddDialog = false; refresh()
                        }
                    }
                })
            }
        )
    }

    if (exerciseToEdit != null) {
        val ex = exerciseToEdit!!
        var name by remember { mutableStateOf(ex.name) }
        var setsInput by remember { mutableStateOf(ex.defaultSets?.toString() ?: "") }
        var repsInput by remember { mutableStateOf(ex.defaultReps?.toString() ?: "") }
        var rest by remember { mutableStateOf(ex.restSec?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { exerciseToEdit = null },
            title = { Text("Редагувати") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SportTextField(value = name, onValueChange = { name = it }, placeholder = "Назва", modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SportTextField(value = setsInput, onValueChange = { setsInput = it }, placeholder = "Сети", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
                        SportTextField(value = repsInput, onValueChange = { repsInput = it }, placeholder = "Репи", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text))
                    }
                    SportTextField(value = rest, onValueChange = { rest = it }, placeholder = "Відпочинок", modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            },
            confirmButton = {
                SportButton(text = "Зберегти", modifier = Modifier, onClick = {
                    scope.launch {
                        val (fSets, fReps) = parseSetsAndReps(setsInput, repsInput)
                        exerciseDao.insert(ex.copy(name = name, defaultSets = fSets, defaultReps = fReps, restSec = rest.toIntOrNull()))
                        exerciseToEdit = null; refresh()
                    }
                })
            }
        )
    }

    if (exerciseToDelete != null) {
        val ex = exerciseToDelete!!
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Видалити?") },
            text = { Text("Видалити '${ex.name}'?") },
            confirmButton = {
                SportButton(text = "Видалити", color = SportRed, modifier = Modifier, onClick = {
                    scope.launch { exerciseDao.delete(ex); exerciseToDelete = null; refresh() }
                })
            },
            dismissButton = { SportButton(text = "Ні", modifier = Modifier, onClick = { exerciseToDelete = null }) }
        )
    }

    // --- ВІЗУАЛЬНА ЧАСТИНА (без змін) ---
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Бібліотека вправ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) }
        }

        ScrollableTabRow(
            selectedTabIndex = muscleGroups.indexOf(selectedGroup).coerceAtLeast(0),
            containerColor = Color.Transparent,
            divider = {},
            edgePadding = 0.dp
        ) {
            muscleGroups.forEach { group ->
                Tab(selected = selectedGroup == group, onClick = { selectedGroup = group }, text = { Text(group) })
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
            items(filteredExercises) { ex ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), border = BorderStroke(1.dp, Color(0xFFE5E5E5))) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(ex.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(ex.primaryGroup, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = { exerciseToEdit = ex }) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
                        IconButton(onClick = { exerciseToDelete = ex }) { Icon(Icons.Default.Delete, null, tint = SportRed) }
                    }
                }
            }
        }
    }
}