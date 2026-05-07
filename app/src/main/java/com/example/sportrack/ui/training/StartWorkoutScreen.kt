package com.example.sportrack.ui.training

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.AppDatabase
import com.example.sportrack.data.model.DayAssignment
import com.example.sportrack.data.model.Exercise
import com.example.sportrack.data.model.WorkoutLog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartWorkoutScreen() {
    val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Нд")
    val selectedDay = remember { mutableStateOf<String?>(null) }
    val sessionExercises = remember { mutableStateListOf<Exercise>() }
    val sessionActive = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val dayAssignmentDao = remember { db.dayAssignmentDao() }
    val exerciseDao = remember { db.exerciseDao() }

    val exercisesCount = remember { mutableStateOf(0) }

    val backgroundBrush = Brush.verticalGradient(
        listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface)
    )

    fun loadExercisesForDay(day: String, onLoaded: (() -> Unit)? = null) {
        scope.launch {
            try {
                val assignment: DayAssignment? = dayAssignmentDao.getByDay(day)
                val groups = assignment?.groups
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() } ?: emptyList()

                if (groups.isEmpty()) {
                    sessionExercises.clear()
                    exercisesCount.value = 0
                    onLoaded?.invoke()
                    return@launch
                }

                val list = exerciseDao.getByGroups(groups)
                sessionExercises.clear()
                sessionExercises.addAll(list)
                exercisesCount.value = list.size
                onLoaded?.invoke()

                if (list.isEmpty()) {
                    snackbarHostState.showSnackbar("Не знайдено вправ для груп: ${groups.joinToString(", ")}")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Помилка: ${e.message ?: e.toString()}")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Почати тренування", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
                .padding(12.dp)
        ) {
            if (!sessionActive.value) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Оберіть день", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            items(days) { day ->
                                val isSelected = selectedDay.value == day
                                Card(
                                    modifier = Modifier
                                        .widthIn(min = 64.dp)
                                        .height(64.dp)
                                        .clickable {
                                            if (selectedDay.value == day) {
                                                selectedDay.value = null
                                                sessionExercises.clear()
                                                exercisesCount.value = 0
                                            } else {
                                                selectedDay.value = day
                                                loadExercisesForDay(day)
                                            }
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                    colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else CardDefaults.cardColors(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 4.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(day, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                                            if (isSelected) {
                                                Spacer(Modifier.height(4.dp))
                                                Text("${exercisesCount.value} вправ", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val label = selectedDay.value?.let { "Вибрано: $it (${exercisesCount.value} вправ)" } ?: "Оберіть день, щоб почати"
                        Text(label, style = MaterialTheme.typography.bodyLarge)

                        Button(
                            onClick = {
                                val day = selectedDay.value
                                if (day == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Оберіть день для початку") }
                                    return@Button
                                }
                                scope.launch {
                                    val assignment = dayAssignmentDao.getByDay(day)
                                    val groups = assignment?.groups?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
                                    if (groups.isEmpty()) {
                                        snackbarHostState.showSnackbar("Для $day немає призначених груп")
                                        return@launch
                                    }
                                    val ex = exerciseDao.getByGroups(groups)
                                    if (ex.isEmpty()) {
                                        snackbarHostState.showSnackbar("Не знайдено вправ для обраного дня")
                                        return@launch
                                    }
                                    sessionExercises.clear()
                                    sessionExercises.addAll(ex)
                                    exercisesCount.value = ex.size
                                    sessionActive.value = true
                                }
                            },
                            enabled = !sessionActive.value
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Почати")
                            Spacer(Modifier.width(8.dp))
                            Text("Почати")
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                if (sessionExercises.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("Після вибору дня тут з'являться вправи для перегляду.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                "Перелік вправ (${sessionExercises.size}):",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 12.dp, top = 12.dp)
                            )
                            Spacer(Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(sessionExercises) { ex ->
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            "${sessionExercises.indexOf(ex) + 1}. ${ex.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            ex.primaryGroup + (ex.subgroup?.let { " / $it" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                                item { Spacer(modifier = Modifier.height(12.dp)) }
                            }
                        }
                    }
                }
            } else {
                WorkoutSessionScreen(
                    exercises = sessionExercises.toList(),
                    onFinish = {
                        sessionActive.value = false
                        sessionExercises.clear()
                        exercisesCount.value = 0
                    }
                )
            }
        }
    }
}

@Composable
fun WorkoutSessionScreen(
    exercises: List<Exercise>,
    onFinish: () -> Unit
) {
    var index by remember { mutableStateOf(0) }
    val ex = exercises.getOrNull(index)

    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }
    var lastLog by remember { mutableStateOf<WorkoutLog?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val logDao = remember { db.workoutLogDao() }

    LaunchedEffect(ex) {
        if (ex != null) {
            repsInput = ex.defaultReps?.toString() ?: ""
            weightInput = ""
            lastLog = logDao.getLastLogForExercise(ex.name)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Сесія тренування", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (ex == null) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text("Немає вправ", style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(onClick = onFinish) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Вихід")
                Spacer(Modifier.width(6.dp))
                Text("Вийти")
            }
            return@Column
        }

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                Text("${index + 1} / ${exercises.size}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                Text(ex.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                if (lastLog != null) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Минулий раз: ${lastLog?.weight} кг x ${lastLog?.reps} репів",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text("Група: ${ex.primaryGroup}${ex.subgroup?.let { " — $it" } ?: ""}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))

                if (ex.isTimed) {
                    Text("Тривалість: ${ex.defaultDurationSec ?: "—"} сек", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("План: ${ex.defaultSets ?: "—"} сетів по ${ex.defaultReps ?: "—"} повторень", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(8.dp))
                Text("Відпочинок: ${ex.restSec ?: "—"} сек", style = MaterialTheme.typography.bodyMedium)

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "📝 Записати в щоденник",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Вкажи фактичну вагу та повторення.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Вага (кг)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { weightInput = it },
                                placeholder = { Text("Напр. 50") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Повторення", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = repsInput,
                                onValueChange = { repsInput = it },
                                placeholder = { Text("Напр. 10") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = { if (index > 0) index-- },
                enabled = index > 0
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Попередня")
                Spacer(Modifier.width(4.dp))
                Text("Назад")
            }

            Row {
                OutlinedButton(onClick = {
                    scope.launch {
                        val w = weightInput.toDoubleOrNull() ?: 0.0
                        val r = repsInput.toIntOrNull() ?: 0
                        val s = ex.defaultSets ?: 1

                        if (w > 0 || r > 0) {
                            logDao.insertLog(WorkoutLog(
                                date = System.currentTimeMillis(), // ДОДАНО
                                exerciseName = ex.name,
                                weight = w,
                                reps = r,
                                sets = s
                            ))
                            Toast.makeText(context, "Результат збережено", Toast.LENGTH_SHORT).show()
                        }
                        onFinish()
                    }
                }) {
                    Text("Вийти")
                }
                Spacer(Modifier.width(8.dp))

                Button(onClick = {
                    scope.launch {
                        val w = weightInput.toDoubleOrNull() ?: 0.0
                        val r = repsInput.toIntOrNull() ?: 0
                        val s = ex.defaultSets ?: 1

                        if (w > 0 || r > 0) {
                            logDao.insertLog(WorkoutLog(
                                date = System.currentTimeMillis(), // ДОДАНО
                                exerciseName = ex.name,
                                weight = w,
                                reps = r,
                                sets = s
                            ))
                            Toast.makeText(context, "Результат збережено", Toast.LENGTH_SHORT).show()
                        }

                        if (index < exercises.lastIndex) {
                            index++
                        } else {
                            onFinish()
                        }
                    }
                }) {
                    Text(if (index == exercises.lastIndex) "Завершити" else "Далі")
                    Spacer(Modifier.width(4.dp))
                    Icon(if (index == exercises.lastIndex) Icons.Default.ExitToApp else Icons.Default.ArrowForward, contentDescription = null)
                }
            }
        }
    }
}