package com.example.planttracker.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.ReferencePlant
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current // ← для снятия фокуса
    val db = AppDatabase.getInstance(context)
    val referencePlantDao = db.referencePlantDao()

    var allPlants by remember { mutableStateOf<List<ReferencePlant>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPlant by remember { mutableStateOf<ReferencePlant?>(null) }

    LaunchedEffect(Unit) {
//        allPlants = referencePlantDao.getAllPlants()
        val plants = referencePlantDao.getAllPlants()
        println("DEBUG: Загружено растений: ${plants.size}")
        allPlants = plants
    }


    val filteredPlants = allPlants.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
//            TopAppBar(title = { Text("Справочник") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Поиск растения") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            // Снимаем фокус → клавиатура скрывается
                            focusManager.clearFocus()
                            // Поиск уже работает в реальном времени — ничего дополнительно не делаем
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Завершить поиск"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (filteredPlants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ничего не найдено")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredPlants) { plant ->
                        ReferencePlantCard(
                            plant = plant,
                            onClick = { selectedPlant = plant }
                        )
                    }
                }
            }
        }

        selectedPlant?.let { plant ->
            PlantDetailDialog(
                plant = plant,
                onDismiss = { selectedPlant = null }
            )
        }
    }
}

// Плитка растения (в сетке)
@Composable
fun ReferencePlantCard(
    plant: ReferencePlant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = plant.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Полив: раз в ${plant.wateringIntervalDays} дн.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = plant.lightInfo,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Диалог с полной информацией
@Composable
fun PlantDetailDialog(
    plant: ReferencePlant,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.headlineSmall
                )

                Text("💧 Полив: раз в ${plant.wateringIntervalDays} дней")
                Text("☀️ Свет: ${plant.lightInfo}")
                Text("🌱 Удобрение: раз в ${plant.fertilizerIntervalDays} дней")

                if (plant.description.isNotEmpty()) {
                    Text(
                        text = plant.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}