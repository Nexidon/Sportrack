package com.example.sportrack.ui.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.AppDatabase
import com.example.sportrack.data.model.WorkoutLog
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDiaryScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }
    val logDao = remember { db.workoutLogDao() }

    var logs by remember { mutableStateOf(emptyList<WorkoutLog>()) }
    var logToDelete by remember { mutableStateOf<WorkoutLog?>(null) }

    fun refreshLogs() {
        scope.launch { logs = logDao.getAllLogs() }
    }

    LaunchedEffect(Unit) { refreshLogs() }

    val groupedLogs = remember(logs) {
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        logs.groupBy { formatter.format(Date(it.date)) }
    }

    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Видалити запис?") },
            text = { Text("Ви впевнені, що хочете видалити цей результат з історії?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        logDao.deleteLog(logToDelete!!)
                        logToDelete = null
                        refreshLogs()
                    }
                }) { Text("Видалити", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { logToDelete = null }) { Text("Скасувати") } }
        )
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Щоденник") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(12.dp)) {
            if (logs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Записів ще немає. Тренуйтеся більше!")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    groupedLogs.forEach { (date, dayLogs) ->
                        item {
                            Text(
                                text = date,
                                modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)).padding(8.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(dayLogs) { log ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(log.exerciseName, fontWeight = FontWeight.Bold)
                                        Text("${log.weight} кг x ${log.reps}", color = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { logToDelete = log }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}