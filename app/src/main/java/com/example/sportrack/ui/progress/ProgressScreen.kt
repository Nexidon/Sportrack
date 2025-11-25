package com.example.sportrack.ui.progress

import androidx.compose.ui.graphics.toArgb
import android.widget.TextView
import android.widget.Toast
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sportrack.data.WeightEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*
import com.example.sportrack.ui.progress.viewmodel.ProgressViewModel
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.MPPointF
import com.example.sportrack.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.sportrack.data.MeasurementEntry
import com.example.sportrack.data.Period
import com.example.sportrack.data.StrengthEntry
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TextField
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow

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
                actions = {
                    IconButton(onClick = { /* Можно добавить refresh action */ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
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

/* ---------------------------
   StrengthsSection
   --------------------------- */

enum class ChartType { LINE, BAR }

@Composable
fun StrengthsSection(viewModel: ProgressViewModel) {
    val context = LocalContext.current
    val strengths by viewModel.allStrengths.collectAsState(initial = emptyList())

    var liftType by remember { mutableStateOf("Усі типи") }
    var weightInput by remember { mutableStateOf("") }
    var repsInput by remember { mutableStateOf("") }
    var newLiftType by remember { mutableStateOf("") }

    var period by remember { mutableStateOf(Period.ALL) }
    var historyCollapsed by remember { mutableStateOf(false) }

    // Елементи керування переглядом прогресу
    var selectedChart by remember { mutableStateOf(ChartType.LINE) }
    var showTrend by remember { mutableStateOf(true) }
    var showGoal by remember { mutableStateOf(true) }

    // Список типів підйомів на основі даних
    val liftTypes = remember(strengths) {
        (strengths.map { it.liftType }.distinct().sorted()).let { list ->
            list
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // -- Введення нового запису --
        item {
            Text("Новий силовий запис", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newLiftType,
                    onValueChange = { newLiftType = it },
                    label = { Text("Тип підйому") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Вага (кг)") },
                    modifier = Modifier.width(120.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = repsInput,
                    onValueChange = { repsInput = it },
                    label = { Text("Повт.") },
                    modifier = Modifier.width(100.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                val w = weightInput.toDoubleOrNull()
                val r = repsInput.toIntOrNull()
                if (newLiftType.isBlank() || w == null || r == null) {
                    Toast.makeText(context, "Заповніть усі поля коректно", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val entry = StrengthEntry(
                    date = System.currentTimeMillis(),
                    liftType = newLiftType.trim(),
                    weight = w,
                    reps = r
                )
                viewModel.saveStrength(entry)
                newLiftType = ""
                weightInput = ""
                repsInput = ""
                Toast.makeText(context, "Запис збережено", Toast.LENGTH_SHORT).show()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Зберегти запис")
            }
        }

        // -- Налаштування фільтра/перегляду --
        item {
            Spacer(Modifier.height(6.dp))
            Text("Перегляд прогресу", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            // Dropdown вибору типу підйому (або "Усі типи")
            var expanded by remember { mutableStateOf(false) }
            val options = listOf("Усі типи") + liftTypes
            Box {
                OutlinedTextField(
                    value = liftType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Тип підйому") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Default.Edit, contentDescription = "Обрати")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { opt ->
                        DropdownMenuItem(text = { Text(opt, maxLines = 1, overflow = TextOverflow.Ellipsis) }, onClick = {
                            liftType = opt
                            expanded = false
                        })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Період
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Period.values().forEach { p ->
                    FilterChip(
                        selected = period == p,
                        onClick = { period = p },
                        label = { Text(p.name.lowercase().replaceFirstChar { it.uppercaseChar() }) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Адаптивний блок: SegmentedButtons + чекбокси (перенос на вузьких екранах)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                if (maxWidth < 420.dp) {
                    // вузький екран — вертикальна компоновка
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SegmentedButtons(selectedChart) { selectedChart = it }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = showTrend, onCheckedChange = { showTrend = it })
                                Text("Показати тренд", modifier = Modifier.padding(start = 4.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = showGoal, onCheckedChange = { showGoal = it })
                                Text("Показати ціль", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                } else {
                    // широкий екран — все в один ряд, чекбокси вирівняні праворуч
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.wrapContentWidth()) {
                            SegmentedButtons(selectedChart) { selectedChart = it }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                            Checkbox(checked = showTrend, onCheckedChange = { showTrend = it })
                            Text("Показати тренд", modifier = Modifier.padding(start = 4.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.wrapContentWidth()) {
                            Checkbox(checked = showGoal, onCheckedChange = { showGoal = it })
                            Text("Показати ціль", modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        // -- Графік прогресу --
        item {
            val filtered = remember(strengths, period) {
                viewModel.filterStrengthsForPeriod(strengths, period)
            }

            // далі фільтруємо по liftType якщо обраний конкретний тип
            val filteredByType = remember(filtered, liftType) {
                if (liftType == "Усі типи") filtered else filtered.filter { it.liftType == liftType }
            }

            when (selectedChart) {
                ChartType.LINE -> {
                    StrengthLineProgress(
                        entries = filteredByType,
                        liftType = if (liftType == "Усі типи") null else liftType,
                        showTrend = showTrend,
                        showGoal = showGoal,
                        viewModel = viewModel
                    )
                }
                ChartType.BAR -> {
                    StrengthBarProgress(
                        entries = filteredByType,
                        viewModel = viewModel
                    )
                }
            }
        }

        // -- Історія записів --
        item {
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("Історія записів:", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { historyCollapsed = !historyCollapsed }) {
                    Text(if (historyCollapsed) "Показати" else "Сховати")
                }
            }
        }

        if (!historyCollapsed) {
            items(strengths) { entry ->
                StrengthEntryItem(
                    entry = entry,
                    onDelete = { viewModel.deleteStrength(it) },
                    onUpdate = { viewModel.updateStrength(it) }
                )
            }
        }
    }
}


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


@Composable
fun StrengthLineProgress(
    entries: List<StrengthEntry>,
    liftType: String?,
    showTrend: Boolean,
    showGoal: Boolean,
    viewModel: ProgressViewModel
) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text("Немає даних для графіка")
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()
    val tertiaryColor = MaterialTheme.colorScheme.tertiary.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    val sorted = remember(entries) { entries.sortedBy { it.date } }
    val labels = remember(sorted) { sorted.map { SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date(it.date)) } }
    val lineEntries = remember(sorted) { sorted.mapIndexed { idx, e -> Entry(idx.toFloat(), e.weight.toFloat()) } }
    val trendEntries = remember(lineEntries, showTrend) {
        if (showTrend && lineEntries.size >= 2) calcMeasurementTrend(lineEntries) else emptyList()
    }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(8.dp),
        factory = { ctx ->
            LineChart(ctx).apply {
                description = Description().apply { text = "" }
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisRight.isEnabled = false
                legend.isEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { chart ->
            val dataSet = LineDataSet(lineEntries, (liftType ?: "Усі типи")).apply {
                setDrawValues(false)
                setDrawCircles(true)
                lineWidth = 2f
                color = primaryColor
                setCircleColor(primaryColor)
            }

            val sets = mutableListOf<ILineDataSet>(dataSet)

            if (showTrend && trendEntries.isNotEmpty()) {
                val trendSet = LineDataSet(trendEntries, "Тренд").apply {
                    setDrawCircles(false)
                    lineWidth = 2f
                    color = secondaryColor
                    setDrawValues(false)
                }
                sets.add(trendSet)
            }

            chart.data = LineData(sets)

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in labels.indices) labels[idx] else ""
                }
            }
            chart.xAxis.setLabelCount(labels.size.coerceAtMost(6), true)
            chart.axisLeft.setDrawGridLines(false)
            chart.xAxis.setDrawGridLines(false)

            if (showGoal && liftType != null) {
                val goal = viewModel.getGoalFor("lift_$liftType")
                if (goal != null) {
                    chart.axisLeft.removeAllLimitLines()
                    val ll = LimitLine(goal.toFloat(), "Ціль").apply {
                        lineWidth = 2f
                        lineColor = tertiaryColor
                        textColor = onSurfaceColor
                    }
                    chart.axisLeft.addLimitLine(ll)
                    val yMin = (lineEntries.minOfOrNull { it.y } ?: 0f).coerceAtMost(goal.toFloat()) - 1f
                    val yMax = (lineEntries.maxOfOrNull { it.y } ?: 0f).coerceAtLeast(goal.toFloat()) + 1f
                    chart.axisLeft.axisMinimum = yMin
                    chart.axisLeft.axisMaximum = yMax
                }
            } else {
                chart.axisLeft.removeAllLimitLines()
            }

            chart.invalidate()
        }
    )
}

@Composable
fun StrengthBarProgress(entries: List<StrengthEntry>, viewModel: ProgressViewModel) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text("Немає даних для графіка")
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val tonnageByDay = remember(entries) {
        viewModel.sumTonnageByDay(entries)
    }

    val labels = remember(tonnageByDay) {
        tonnageByDay.keys.map { SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(it)) }
    }
    val values = remember(tonnageByDay) { tonnageByDay.values.toList() }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(8.dp),
        factory = { ctx ->
            BarChart(ctx).apply {
                description = Description().apply { text = "" }
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                legend.isEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { chart ->
            val entriesBar = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
            val set = BarDataSet(entriesBar, "Тонаж за днями").apply {
                valueTextSize = 10f
                setDrawValues(true)
                color = primaryColor
            }
            val data = BarData(set)
            data.barWidth = 0.7f
            chart.data = data

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in labels.indices) labels[idx] else ""
                }
            }

            chart.xAxis.setDrawGridLines(false)
            chart.axisLeft.setDrawGridLines(false)
            chart.invalidate()
        }
    )
}

@Composable
fun StrengthChart(entries: List<StrengthEntry>, viewModel: ProgressViewModel) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
            Text("Немає даних для графіка")
        }
        return
    }

    val tonnage = viewModel.sumTonnageByLiftType(entries)
    val labels = tonnage.keys.toList()
    val values = tonnage.values.toList()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(8.dp),
        factory = { ctx ->
            BarChart(ctx).apply {
                description = Description().apply { text = "" }
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.granularity = 1f
                legend.isEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { chart ->
            val entriesBar = values.mapIndexed { i, v -> BarEntry(i.toFloat(), v.toFloat()) }
            val set = BarDataSet(entriesBar, "Тонаж").apply {
                valueTextSize = 10f
                setDrawValues(true)
                color = Color(0xFF4CAF50).toArgb()
            }
            val data = BarData(set)
            data.barWidth = 0.7f

            chart.data = data

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt()
                    return if (idx in labels.indices) labels[idx] else ""
                }
            }
            chart.xAxis.setDrawGridLines(false)
            chart.axisLeft.setDrawGridLines(false)

            chart.invalidate()
        }
    )
}

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

/* ---------------------------
   WeightSection
   --------------------------- */
@Composable
fun WeightSection(viewModel: ProgressViewModel) {
    val context = LocalContext.current
    var weightInput by remember { mutableStateOf("") }
    var showTrend by remember { mutableStateOf(false) }
    var historyCollapsed by remember { mutableStateOf(false) }
    var sortDescending by remember { mutableStateOf(true) }
    var showGoalDialog by remember { mutableStateOf(false) }

    val entries by viewModel.allEntries.collectAsState(initial = emptyList())
    val goal by viewModel.goal.collectAsState()

    val sortedEntries = if (sortDescending) entries.sortedByDescending { it.date }
    else entries.sortedBy { it.date }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = weightInput,
                onValueChange = { weightInput = it },
                label = { Text("Вага (кг)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    weightInput.toDoubleOrNull()?.let { value ->
                        viewModel.saveWeight(value)
                        weightInput = ""
                        Toast.makeText(context, "Вага збережена!", Toast.LENGTH_SHORT).show()
                    } ?: Toast.makeText(context, "Введіть коректну вагу", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Зберегти вагу") }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = goal?.let { "Поточна ціль: ${it} кг" } ?: "Ціль не встановлена",
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = { showGoalDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Змінити ціль")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = showTrend, onCheckedChange = { showTrend = it })
                Text("Показати трендову лінію")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Графік зміни ваги:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            WeightChart(entries, goal, showTrend)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Історія ваги:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = { historyCollapsed = !historyCollapsed }) {
                    Text(if (historyCollapsed) "Показати історію" else "Сховати історію")
                }
                Button(onClick = { sortDescending = !sortDescending }) {
                    Text(if (sortDescending) "Спочатку нові" else "Спочатку старі")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            AnimatedVisibility(
                visible = !historyCollapsed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    sortedEntries.forEach { entry ->
                        WeightEntryItem(
                            entry = entry,
                            onDelete = { viewModel.deleteWeight(it) },
                            onUpdate = { updatedEntry -> viewModel.updateWeight(updatedEntry) }
                        )
                    }
                }
            }
        }
    }
}

fun calcMeasurementTrend(entries: List<Entry>): List<Entry> {
    if (entries.size < 2) return emptyList()

    val xs = entries.map { it.x.toDouble() }
    val ys = entries.map { it.y.toDouble() }

    val xMean = xs.average()
    val yMean = ys.average()

    var numerator = 0.0
    var denominator = 0.0
    for (i in xs.indices) {
        numerator += (xs[i] - xMean) * (ys[i] - yMean)
        denominator += (xs[i] - xMean) * (xs[i] - xMean)
    }

    val slope = numerator / denominator
    val intercept = yMean - slope * xMean

    return xs.map { x -> Entry(x.toFloat(), (slope * x + intercept).toFloat()) }
}


@Composable
fun MeasurementChart(
    measurements: List<MeasurementEntry>,
    valueSelector: (MeasurementEntry) -> Double?,
    label: String,
    lineColor: Color,
    showTrend: Boolean = false,
    goal: Double? = null
) {
    if (measurements.isEmpty()) return

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(8.dp),
        factory = { context -> /* оригинальная фабрика */ LineChart(context).apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            axisRight.isEnabled = false

            setBackgroundColor(android.graphics.Color.BLACK)
            xAxis.textColor = android.graphics.Color.WHITE
            axisLeft.textColor = android.graphics.Color.WHITE
            xAxis.gridColor = android.graphics.Color.DKGRAY
            axisLeft.gridColor = android.graphics.Color.DKGRAY
            legend.textColor = android.graphics.Color.WHITE
        } },
        update = { chart ->
            val entries = measurements.mapIndexedNotNull { index, entry -> valueSelector(entry)?.let { Entry(index.toFloat(), it.toFloat()) } }
            if (entries.isEmpty()) return@AndroidView

            val dataSet = LineDataSet(entries, label).apply {
                mode = LineDataSet.Mode.LINEAR
                color = lineColor.toArgb()
                setCircleColor(lineColor.toArgb())
                lineWidth = 2f
                circleRadius = 4f
                setDrawValues(false)
            }

            val sets = mutableListOf<ILineDataSet>(dataSet)

            goal?.let {
                val goalEntries = listOf(
                    Entry(0f, it.toFloat()),
                    Entry(entries.size - 1f, it.toFloat())
                )
                val goalSet = LineDataSet(goalEntries, "Ціль").apply {
                    color = Color.Red.toArgb()
                    lineWidth = 2f
                    setDrawCircles(false)
                    enableDashedLine(10f, 5f, 0f)
                }
                sets.add(goalSet)
            }

            if (showTrend && entries.size >= 2) {
                val trendEntries = calcMeasurementTrend(entries)
                val trendSet = LineDataSet(trendEntries, "Тренд").apply {
                    mode = LineDataSet.Mode.LINEAR
                    color = Color.Gray.toArgb()
                    lineWidth = 2f
                    setDrawCircles(false)
                    setDrawValues(false)
                }
                sets.add(trendSet)
            }

            chart.data = LineData(sets)
            val yValues = entries.map { it.y } + listOfNotNull(goal?.toFloat())
            chart.axisLeft.axisMinimum = (yValues.minOrNull() ?: 0f) - 5f
            chart.axisLeft.axisMaximum = (yValues.maxOrNull() ?: 0f) + 5f

            chart.invalidate()
        }
    )
}

@Composable
fun MeasurementsSection(viewModel: ProgressViewModel) {
    val context = LocalContext.current
    val measurements: List<MeasurementEntry> by viewModel.allMeasurements.collectAsState(initial = emptyList())

    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }
    var biceps by remember { mutableStateOf("") }

    var showTrend by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = showTrend, onCheckedChange = { showTrend = it })
        Text("Показати трендову лінію")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Новий замір:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(value = chest, onValueChange = { chest = it }, label = { Text("Груди (см)") })
            OutlinedTextField(value = waist, onValueChange = { waist = it }, label = { Text("Талія (см)") })
            OutlinedTextField(value = hips, onValueChange = { hips = it }, label = { Text("Стегна (см)") })
            OutlinedTextField(value = biceps, onValueChange = { biceps = it }, label = { Text("Біцепс (см)") })

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                val entry = MeasurementEntry(
                    date = System.currentTimeMillis(),
                    chest = chest.toDoubleOrNull(),
                    waist = waist.toDoubleOrNull(),
                    hips = hips.toDoubleOrNull(),
                    biceps = biceps.toDoubleOrNull()
                )
                viewModel.saveMeasurement(entry)
                chest = ""
                waist = ""
                hips = ""
                biceps = ""
                Toast.makeText(context, "Замір збережено!", Toast.LENGTH_SHORT).show()
            }) {
                Text("Зберегти замір")
            }
        }

        item {
            Text("Графіки замірів:", style = MaterialTheme.typography.titleMedium)
            MeasurementChart(measurements, { it.chest }, "Груди", Color.Cyan, showTrend = showTrend)
            MeasurementChart(measurements, { it.waist }, "Талія", Color.Green, showTrend = showTrend)
            MeasurementChart(measurements, { it.hips }, "Стегна", Color.Magenta, showTrend = showTrend)
            MeasurementChart(measurements, { it.biceps }, "Біцепс", Color.Yellow, showTrend = showTrend)
        }

        item {
            Text("Історія замірів:", style = MaterialTheme.typography.titleMedium)
        }

        items(measurements) { entry ->
            MeasurementEntryItem(
                entry,
                onDelete = { viewModel.deleteMeasurement(it) },
                onUpdate = { viewModel.updateMeasurement(it) }
            )
        }
    }
}

@Composable
fun WeightChart(entries: List<WeightEntry>, goal: Double?, showTrend: Boolean) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                description = Description().apply { text = "" }
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisRight.isEnabled = false
                legend.isEnabled = false

                setBackgroundColor(Color.Black.toArgb())
                xAxis.textColor = Color.White.toArgb()
                axisLeft.textColor = Color.White.toArgb()
                legend.textColor = Color.White.toArgb()
                xAxis.gridColor = Color.Gray.toArgb()
                axisLeft.gridColor = Color.Gray.toArgb()

                marker = object : MarkerView(context, R.layout.marker_weight) {
                    private val tvContent: TextView = findViewById(R.id.tvContent)

                    override fun refreshContent(
                        e: Entry?,
                        highlight: Highlight?
                    ) {
                        tvContent.text = e?.y?.let { "$it кг" } ?: ""
                        super.refreshContent(e, highlight)
                    }

                    override fun getOffset(): MPPointF {
                        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
                    }
                }
            }
        },
        update = { chart ->
            val lineEntries = entries.mapIndexed { index, e -> Entry(index.toFloat(), e.weight.toFloat()) }

            val dataSet = LineDataSet(lineEntries, "Вага").apply {
                setDrawValues(false)
                setDrawCircles(true)
                lineWidth = 2f
                color = Color.Cyan.toArgb()
                setCircleColor(Color.Magenta.toArgb())
            }

            val dataSets = mutableListOf<LineDataSet>(dataSet)

            if (showTrend && entries.size >= 2) {
                val trendEntries = calcTrend(entries)
                val trendSet = LineDataSet(trendEntries, "Тренд").apply {
                    setDrawValues(false)
                    setDrawCircles(false)
                    lineWidth = 2f
                    color = Color.Red.toArgb()
                }
                dataSets.add(trendSet)
            }

            chart.data = LineData(dataSets.map { it as ILineDataSet })

            chart.axisLeft.removeAllLimitLines()

            goal?.let {
                val ll = LimitLine(it.toFloat(), "Ціль").apply {
                    lineWidth = 2f
                    lineColor = Color.Green.toArgb()
                    textColor = Color.White.toArgb()
                }
                chart.axisLeft.addLimitLine(ll)

                val currentMin = (entries.minOfOrNull { it.weight }?.toFloat() ?: 0f).coerceAtMost(it.toFloat())
                val currentMax = (entries.maxOfOrNull { it.weight }?.toFloat() ?: 0f).coerceAtLeast(it.toFloat())
                chart.axisLeft.axisMinimum = currentMin - 1f
                chart.axisLeft.axisMaximum = currentMax + 1f
            }

            chart.invalidate()
        }

    )
}

fun calcTrend(entries: List<WeightEntry>): List<Entry> {
    if (entries.size < 2) return emptyList()

    val xs = entries.indices.map { it.toDouble() }
    val ys = entries.map { it.weight.toDouble() }

    val xMean = xs.average()
    val yMean = ys.average()

    var numerator = 0.0
    var denominator = 0.0
    for (i in xs.indices) {
        numerator += (xs[i] - xMean) * (ys[i] - yMean)
        denominator += (xs[i] - xMean) * (xs[i] - xMean)
    }

    val slope = numerator / denominator
    val intercept = yMean - slope * xMean

    return xs.map { x -> Entry(x.toFloat(), (slope * x + intercept).toFloat()) }
}

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