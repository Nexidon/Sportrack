package com.example.sportrack.ui.training

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen() {
    val tabs = listOf("Розклад", "Почати тренування")
    val selectedTab = remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(8.dp))

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
        }
    }
}
