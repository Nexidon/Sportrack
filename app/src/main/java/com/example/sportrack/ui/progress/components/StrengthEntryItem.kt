package com.example.sportrack.ui.progress.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.model.StrengthEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StrengthEntryItem(
    entry: StrengthEntry,
    onDelete: (StrengthEntry) -> Unit,
    onUpdate: (StrengthEntry) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    var formattedDate by remember { mutableStateOf(dateFormat.format(Date(entry.date))) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showEditDialog) {
        var lift by remember { mutableStateOf(entry.liftType) }
        var w by remember { mutableStateOf(entry.weight.toString()) }
        var r by remember { mutableStateOf(entry.reps.toString()) }
        var dateText by remember { mutableStateOf(formattedDate) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редагувати запис") },
            text = {
                Column {
                    OutlinedTextField(value = lift, onValueChange = { lift = it }, label = { Text("Тип підйому") }, singleLine = true)
                    OutlinedTextField(value = w, onValueChange = { w = it }, label = { Text("Вага (кг)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = r, onValueChange = { r = it }, label = { Text("Повт.") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = dateText, onValueChange = { dateText = it }, label = { Text("Дата (дд.MM.yyyy HH:mm)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val parsedWeight = w.toDoubleOrNull()
                    val parsedReps = r.toIntOrNull()
                    val parsedDate = try { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).parse(dateText)?.time } catch (e: Exception) { null }

                    if (parsedWeight != null && parsedReps != null && parsedDate != null) {
                        onUpdate(entry.copy(
                            liftType = lift,
                            weight = parsedWeight,
                            reps = parsedReps,
                            date = parsedDate
                        ))
                        formattedDate = dateText
                        showEditDialog = false
                    }
                }) { Text("Зберегти") }
            },
            dismissButton = { Button(onClick = { showEditDialog = false }) { Text("Скасувати") } }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Видалити запис?") },
            text = { Text("Видалити запис від $formattedDate ?") },
            confirmButton = { Button(onClick = { onDelete(entry); showDeleteConfirm = false }) { Text("Видалити") } },
            dismissButton = { Button(onClick = { showDeleteConfirm = false }) { Text("Скасувати") } }
        )
    }

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text("${entry.liftType} — ${entry.weight} кг x ${entry.reps}")
                Text("Дата: $formattedDate", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Редагувати")
            }
            Button(onClick = { showDeleteConfirm = true }) {
                Text("Видалити")
            }
        }
    }
}
