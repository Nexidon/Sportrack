package com.example.sportrack.ui.progress.components

import android.R.attr.fontWeight
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.sportrack.data.model.WeightEntry
import com.example.sportrack.ui.components.SportButton
import com.example.sportrack.ui.theme.SportRed
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WeightEntryItem(
    entry: WeightEntry,
    onDelete: (WeightEntry) -> Unit,
    onUpdate: (WeightEntry) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(entry.date))

    // Состояния для диалогов
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // --- ДИАЛОГ РЕДАКТИРОВАНИЯ ---
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
                        label = { Text("Дата (дд.ММ.yyyy HH:mm)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                SportButton(
                    onClick = {
                        val parsedWeight = newWeight.toDoubleOrNull()
                        val parsedDate = try {
                            dateFormat.parse(newDate)?.time
                        } catch (e: Exception) {
                            null
                        }

                        if (parsedWeight != null && parsedDate != null) {
                            onUpdate(entry.copy(weight = parsedWeight, date = parsedDate))
                            showEditDialog = false
                        }
                    },
                    text = "Зберегти", modifier = Modifier,
                )


            },
            dismissButton = {
                SportButton(onClick = { showEditDialog = false }, text = "Скасувати", color = SportRed, modifier = Modifier)
            }
        )
    }

    // --- ДИАЛОГ УДАЛЕНИЯ ---
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Видалити запис?", fontWeight = FontWeight.Bold) },
            text = { Text("Ви впевнені, що хочете видалити запис від $formattedDate?") },
            confirmButton = {
                SportButton(
                    modifier = Modifier,
                    text = "Видалити",
                    onClick = {
                        onDelete(entry)
                        showDeleteConfirm = false
                    },
                    color = SportRed
                )
            },
            dismissButton = {
                SportButton(onClick = { showDeleteConfirm = false }, text = "Скасувати", modifier = Modifier)
            }
        )
    }

    // --- КРАСИВАЯ КАРТОЧКА ---
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp), // Небольшой отступ между карточками
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Левая часть: Данные
            Column {
                Text(
                    text = "${entry.weight} кг",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Правая часть: Кнопки
            Row {
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SportRed)
                }
            }
        }
    }
}