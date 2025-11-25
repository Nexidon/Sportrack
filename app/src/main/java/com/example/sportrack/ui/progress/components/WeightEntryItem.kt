package com.example.sportrack.ui.progress.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.model.WeightEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeightEntryItem(
    entry: WeightEntry,
    onDelete: (WeightEntry) -> Unit,
    onUpdate: (WeightEntry) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    var formattedDate by remember { mutableStateOf(dateFormat.format(Date(entry.date))) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showEditDialog) {
        var newWeight by remember { mutableStateOf(entry.weight.toString()) }
        var newDate by remember { mutableStateOf(formattedDate) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редагувати запис") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newWeight,
                        onValueChange = { newWeight = it },
                        label = { Text("Вага (кг)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDate,
                        onValueChange = { newDate = it },
                        label = { Text("Дата (дд.MM.yyyy HH:mm)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val parsedWeight = newWeight.toDoubleOrNull()
                    val parsedDate = try { dateFormat.parse(newDate)?.time } catch (e: Exception) { null }

                    if (parsedWeight != null && parsedDate != null) {
                        onUpdate(entry.copy(weight = parsedWeight, date = parsedDate))
                        formattedDate = newDate
                        showEditDialog = false
                    }
                }) { Text("Зберегти") }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) { Text("Скасувати") }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Видалити запис?") },
            text = { Text("Ви впевнені, що хочете видалити запис від $formattedDate?") },
            confirmButton = {
                Button(onClick = {
                    onDelete(entry)
                    showDeleteConfirm = false
                }) { Text("Видалити") }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirm = false }) { Text("Скасувати") }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = "Дата: $formattedDate", color = colorScheme.onSurface)
            Text(text = "Вага: ${entry.weight} кг", color = colorScheme.onSurface)
        }
        Row {
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Редагувати запис")
            }
            Button(onClick = { showDeleteConfirm = true }) {
                Text("Видалити")
            }
        }
    }
}