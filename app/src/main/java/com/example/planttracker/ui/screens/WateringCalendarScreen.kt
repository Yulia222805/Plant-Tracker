package com.example.planttracker.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.Plant
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WateringCalendarScreen() {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val plantDao = db.plantDao()

    val plants by plantDao.getAllPlants().collectAsState(initial = emptyList())

    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)

    val wateringMap = remember(plants, currentYear, currentMonth) {
        mutableMapOf<Int, MutableList<Plant>>().apply {
            for (plant in plants) {
                var nextWater = Calendar.getInstance().apply {
                    timeInMillis = plant.lastWatered
                }
                // Сразу переходим к следующему поливу
                nextWater.add(Calendar.DAY_OF_MONTH, plant.wateringIntervalDays)

                repeat(30) {
                    if (nextWater.get(Calendar.YEAR) == currentYear &&
                        nextWater.get(Calendar.MONTH) == currentMonth
                    ) {
                        val day = nextWater.get(Calendar.DAY_OF_MONTH)
                        getOrPut(day) { mutableListOf() }.add(plant)
                    }
                    nextWater.add(Calendar.DAY_OF_MONTH, plant.wateringIntervalDays)
                }
            }
        }
    }

    var selectedDayPlants by remember { mutableStateOf<List<Plant>?>(null) }
    var selectedDayOfMonth by remember { mutableStateOf<Int?>(null) }
    var showWateringDialog by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
        "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
    )
    val monthName = monthNames[currentMonth]

    val scope = rememberCoroutineScope()

    Scaffold(
//        topBar = { TopAppBar(title = { Text("Календарь полива") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = "$monthName $currentYear",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach {
                    Text(it, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val firstDayOfMonth = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, 1)
                }.get(Calendar.DAY_OF_WEEK) - 2

                items(firstDayOfMonth) {
                    Spacer(modifier = Modifier.size(32.dp))
                }

                val daysInMonth = Calendar.getInstance().apply {
                    set(currentYear, currentMonth, 1)
                }.getActualMaximum(Calendar.DAY_OF_MONTH)

                items(daysInMonth) { dayIndex ->
                    val day = dayIndex + 1
                    val isToday = isSameDay(day, currentMonth, currentYear)
                    val plantsForDay = wateringMap[day]
                    val isWateringDay = plantsForDay != null && plantsForDay.isNotEmpty()

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isWateringDay -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            )
                            .then(
                                if (isWateringDay) {
                                    Modifier.clickable {
                                        selectedDayPlants = plantsForDay
                                        selectedDayOfMonth = day
                                        showWateringDialog = true
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$day",
                            color = if (isWateringDay) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            fontSize = 12.sp,
                            fontWeight = if (isWateringDay) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Показ диалога
        if (showWateringDialog && selectedDayPlants != null && selectedDayOfMonth != null) {
            WateringPlantsDialog(
                plants = selectedDayPlants!!,
                selectedYear = currentYear,
                selectedMonth = currentMonth,
                selectedDay = selectedDayOfMonth!!,
                onWater = { plant ->
                    println("💧 DEBUG: plant.id = ${plant.id}")
                    scope.launch {
                        val today = System.currentTimeMillis()
                        println("💧 DEBUG: Обновляем lastWatered = $today")
                        plantDao.update(plant.copy(lastWatered = today))
                        println("💧 DEBUG: Обновление отправлено в DAO")
                    }
                },
                onDismiss = {
                    showWateringDialog = false
                    selectedDayPlants = null
                    selectedDayOfMonth = null
                }
            )
        }
    }
}

@Composable
fun WateringPlantsDialog(
    plants: List<Plant>,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onWater: (Plant) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val now = Calendar.getInstance()
    val isTodaySelected = (now.get(Calendar.YEAR) == selectedYear &&
            now.get(Calendar.MONTH) == selectedMonth &&
            now.get(Calendar.DAY_OF_MONTH) == selectedDay)

    // Состояние: какие растения уже "политы" в этом диалоге
    var wateredPlantIds by remember { mutableStateOf<Set<Long>>(emptySet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Растения для полива") },
        text = {
            LazyColumn {
                items(items = plants) { plant ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plant.name,
                            maxLines = 1,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )

                        if (!isTodaySelected) {
                            // Не сегодня — показываем "ещё рано"
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .clickable {
                                        Toast.makeText(
                                            context,
                                            "Можно поливать только сегодня",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            ) {
                                Text(
                                    text = "Еще рано",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            // Сегодня — показываем кнопку
                            if (plant.id in wateredPlantIds) {
                                Button(
                                    onClick = {}, // ← ДОБАВЬ ЭТО!
                                    enabled = false,
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text("Полито ✅", fontSize = 10.sp)
                                }
                            } else {
                                // Ещё не полили
                                Button(
                                    onClick = {
                                        onWater(plant)
                                        // Обновляем UI мгновенно
                                        wateredPlantIds = wateredPlantIds + plant.id
                                    },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Полить", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

private fun isSameDay(day: Int, month: Int, year: Int): Boolean {
    val now = Calendar.getInstance()
    return now.get(Calendar.DAY_OF_MONTH) == day &&
            now.get(Calendar.MONTH) == month &&
            now.get(Calendar.YEAR) == year
}

private fun isSameDayAsToday(timestamp: Long): Boolean {
    val now = Calendar.getInstance()
    val selected = Calendar.getInstance().apply {
        timeInMillis = timestamp
    }

    // Обнуляем время у КОПИЙ — чтобы избежать влияния часового пояса
    val nowNormalized = Calendar.getInstance().apply {
        set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val selectedNormalized = Calendar.getInstance().apply {
        set(selected.get(Calendar.YEAR), selected.get(Calendar.MONTH), selected.get(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return nowNormalized.timeInMillis == selectedNormalized.timeInMillis
}