package com.example.sportrack.ui.progress


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportrack.ui.progress.sections.MeasurementsSection
import com.example.sportrack.ui.progress.sections.StrengthsSection
import com.example.sportrack.ui.progress.sections.WeightSection
import com.example.sportrack.ui.progress.ProgressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Вага", "Обхвати", "Сили")

    // лёгкий фон-градиент
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Прогрес", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(innerPadding)
                    .padding(12.dp)
            ) {
                // Tabs в карточке для визуального контраста
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title) }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Контентные секции обёрнуты в Card + padding
                when (selectedTab) {
                    0 -> {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            WeightSection(viewModel = viewModel)
                        }
                    }
                    1 -> {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            MeasurementsSection(viewModel = viewModel)
                        }
                    }
                    2 -> {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            StrengthsSection(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    )
}

enum class ChartType { LINE, BAR }

@Composable
fun SegmentedButtons(selected: ChartType, onSelect: (ChartType) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val items = listOf(
            ChartType.LINE to "Вага (лінія)",
            ChartType.BAR to "Тонаж (стовпці)"
        )

        items.forEach { (type, label) ->
            val commonMod = Modifier
                .height(36.dp)
                .widthIn(min = 92.dp) // мінімальна ширина, щоб текст не притискався
            if (selected == type) {
                FilledTonalButton(
                    onClick = { onSelect(type) },
                    modifier = commonMod
                ) {
                    Text(text = label, maxLines = 1)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelect(type) },
                    modifier = commonMod
                ) {
                    Text(text = label, maxLines = 1)
                }
            }
        }
    }
}
