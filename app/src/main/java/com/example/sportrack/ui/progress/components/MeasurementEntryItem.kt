package com.example.sportrack.ui.progress.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.model.MeasurementEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeasurementEntryItem(
    entry: MeasurementEntry,
    onDelete: (MeasurementEntry) -> Unit,
    onUpdate: (MeasurementEntry) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    var formattedDate by remember { mutableStateOf(dateFormat.format(Date(entry.date))) }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showEditDialog) {
        var chest by remember { mutableStateOf(entry.chest?.toString() ?: "") }
        var waist by remember { mutableStateOf(entry.waist?.toString() ?: "") }
        var hips by remember { mutableStateOf(entry.hips?.toString() ?: "") }
        var biceps by remember { mutableStateOf(entry.biceps?.toString() ?: "") }
        var dateText by remember { mutableStateOf(formattedDate) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Редагувати запис") },
            text = {
                Column {
                    OutlinedTextField(chest, { chest = it }, label = { Text("Груди (см)") }, singleLine = true)
                    OutlinedTextField(waist, { waist = it }, label = { Text("Талія (см)") }, singleLine = true)
                    OutlinedTextField(hips, { hips = it }, label = { Text("Стегна (см)") }, singleLine = true)
                    OutlinedTextField(biceps, { biceps = it }, label = { Text("Біцепс (см)") }, singleLine = true)
                    OutlinedTextField(dateText, { dateText = it }, label = { Text("Дата (дд.MM.yyyy HH:mm)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(onClick = {
                    val parsedDate = try { dateFormat.parse(dateText)?.time } catch (e: Exception) { null }
                    if (parsedDate != null) {
                        onUpdate(entry.copy(
                            chest = chest.toDoubleOrNull(),
                            waist = waist.toDoubleOrNull(),
                            hips = hips.toDoubleOrNull(),
                            biceps = biceps.toDoubleOrNull(),
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
            text = { Text("Ви впевнені, що хочете видалити запис від $formattedDate?") },
            confirmButton = { Button(onClick = { onDelete(entry); showDeleteConfirm = false }) { Text("Видалити") } },
            dismissButton = { Button(onClick = { showDeleteConfirm = false }) { Text("Скасувати") } }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Дата: $formattedDate")
            entry.chest?.let { Text("Груди: $it см") }
            entry.waist?.let { Text("Талія: $it см") }
            entry.hips?.let { Text("Стегна: $it см") }
            entry.biceps?.let { Text("Біцепс: $it см") }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(onClick = { showEditDialog = true }, modifier = Modifier.width(100.dp)) { Text("Редагувати") }
            Button(onClick = { showDeleteConfirm = true }, modifier = Modifier.width(100.dp)) { Text("Видалити") }
        }
    }
}
