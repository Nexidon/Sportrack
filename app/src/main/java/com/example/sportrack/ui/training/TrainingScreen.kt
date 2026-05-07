package com.example.sportrack.ui.training

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TrainingScreen() {
    // Додали четверту вкладку
    val tabs = listOf("Розклад", "Почати", "Вправи", "Щоденник")
    val selectedTab = remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

        // Якщо вкладок багато, краще використовувати ScrollableTabRow,
        // але для 4 штук звичайний TabRow теж підійде
        TabRow(selectedTabIndex = selectedTab.value) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab.value == index,
                    onClick = { selectedTab.value = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab.value) {
            0 -> ScheduleScreen()
            1 -> StartWorkoutScreen()
            2 -> ExercisesLibraryScreen()
            3 -> WorkoutDiaryScreen() // Виклик нового екрану
        }
    }
}