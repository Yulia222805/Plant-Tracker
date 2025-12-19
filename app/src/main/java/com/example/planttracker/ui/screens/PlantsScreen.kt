//package com.example.planttracker.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.planttracker.database.Plant
//import com.example.planttracker.viewmodel.PlantViewModel
//import java.text.SimpleDateFormat
//import java.util.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//
//import androidx.compose.foundation.background
//
//
//@Composable
//fun MyPlantsScreen(
//    navigateToAddPlant: () -> Unit = {}
//) {
//    PlantsScreenContent(onAddPlantClick = navigateToAddPlant)
//}
//
//@Composable
//fun PlantsScreenContent(
//    onAddPlantClick: () -> Unit
//) {
//    val viewModel: PlantViewModel = hiltViewModel()
//    val plants by viewModel.allPlants.collectAsState()
//
//    Scaffold(
//        topBar = { },
//        floatingActionButton = {
//            FloatingActionButton(onClick = onAddPlantClick) {
//                Icon(Icons.Default.Add, contentDescription = "Добавить растение")
//            }
//        }
//    ) { padding ->
//        if (plants.isEmpty()) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("У вас пока нет растений")
//            }
//        } else {
//            LazyVerticalGrid(
//                columns = GridCells.Adaptive(minSize = 160.dp),
//                contentPadding = padding,
//                modifier = Modifier.fillMaxSize()
//            ) {
//                items(plants.size) { index -> // ✅ Передаём размер списка
//                    val plant = plants[index] // ✅ Берём элемент по индексу
//                    PlantCard(plant = plant)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun PlantCard(plant: Plant) {
//    Card(
//        modifier = Modifier
//            .padding(8.dp)
//            .fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(80.dp)
//                    .background(Color.Green.copy(alpha = 0.2f))
//            ) {
//                Text(
//                    text = "🌿",
//                    modifier = Modifier.align(Alignment.Center),
//                    style = MaterialTheme.typography.headlineMedium
//                )
//            }
//
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(plant.name, fontWeight = FontWeight.Bold)
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(plant.description ?: "Нет описания", style = MaterialTheme.typography.bodySmall)
//
//            val sdf = remember { SimpleDateFormat("dd.MM", Locale.getDefault()) }
//            Text("Полив: ${sdf.format(Date(plant.lastWatered))}", style = MaterialTheme.typography.bodySmall)
//        }
//    }
//}