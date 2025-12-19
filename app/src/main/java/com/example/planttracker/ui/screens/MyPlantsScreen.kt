// ui/screens/MyPlantsScreen.kt
package com.example.planttracker.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.Plant
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.rememberAsyncImagePainter
import com.example.planttracker.ui.components.AnimatedWateringProgress
import com.example.planttracker.workers.CheckWateringWorker
import com.example.planttracker.ui.components.FullScreenImageDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPlantsScreen(
    navigateToAddPlant: () -> Unit,
    navigateToPlantDetail: (Long) -> Unit,
//    onEditPlantClick: (Long) -> Unit,
//    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val plantDao = db.plantDao()

    var plants by remember { mutableStateOf<List<Plant>>(emptyList()) }
    var selectedPlant by remember { mutableStateOf<Plant?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<Plant?>(null) }

    LaunchedEffect(Unit) {
        plantDao.getAllPlants().collectLatest { list ->
            plants = list
        }
    }

    val scope = rememberCoroutineScope()

    Scaffold(
//        topBar = { TopAppBar(title = { Text("Мои растения") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToAddPlant() },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить растение")
            }
        }
    ) { innerPadding ->
        if (plants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет растений. Нажмите +, чтобы добавить первое.")
            }
        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//            ) {
//                items(plants) { plant ->
//                    PlantItem(
//                        plant = plant,
////                        onItemClick = { selectedPlant = plant },
//                        onItemClick = { navigateToPlantDetail(plant.id) },
//                        onDeleteClick = { showDeleteConfirmation = plant }
//
//                    )
//                }
//            }
            // Вместо LazyColumn:
            if (plants.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет растений. Нажмите +, чтобы добавить первое.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(plants) { plant ->
                        MyPlantCard(
                            plant = plant,
                            onItemClick = { navigateToPlantDetail(plant.id) }
//                            onDeleteClick = { showDeleteConfirmation = plant }
                        )
                    }
                }
            }
        }

        // Диалог деталей
//        selectedPlant?.let { plant ->
//            PlantDetailDialog(
//                plant = plant,
//                onEditClick = {
//                    onEditPlantClick(plant.id)
//                    selectedPlant = null
//                },
//                onDeleteClick = {
//                    selectedPlant = null
//                    showDeleteConfirmation = plant
//                },
//                onDismiss = { selectedPlant = null }
//            )
//        }

        // Подтверждение удаления
        showDeleteConfirmation?.let { plant ->
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = null },
                title = { Text("Удалить растение?") },
                text = { Text("Вы уверены, что хотите удалить «${plant.name}»?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch { plantDao.delete(plant) }
                            showDeleteConfirmation = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteConfirmation = null }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }

    // В MyPlantsScreen или HomeScreen

}

@Composable
fun PlantItem(
    plant: Plant,
    onItemClick: () -> Unit,
//    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFullScreenImage by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onItemClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Миниатюра
        if (plant.imagePath != null) {
            val painter = rememberAsyncImagePainter(
                model = android.net.Uri.parse(plant.imagePath),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showFullScreenImage = true }
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Фото ${plant.name}",
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Нет фото",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = plant.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Полив: раз в ${plant.wateringIntervalDays} дн.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val progress = plant.getWateringProgress()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(top = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

//        IconButton(onClick = onDeleteClick) {
//            Icon(Icons.Default.Delete, contentDescription = "Удалить")
//        }
    }

    // Полноэкранный просмотр
//    if (showFullScreenImage && plant.imagePath != null) {
//        FullScreenImageDialog(
//            imageUrl = plant.imagePath,
//            onDismiss = { showFullScreenImage = false }
//        )
//    }

//    AnimatedWateringProgress(
//        progress = plant.getWateringProgress(),
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(4.dp)
//            .padding(top = 4.dp)
//    )
}

// В Plant.kt
fun Plant.getWateringProgress(): Float {
    val now = System.currentTimeMillis()
    val timePassed = now - lastWatered
    val intervalMillis = wateringIntervalDays * 24L * 60L * 60L * 1000L
    return if (intervalMillis <= 0) 1f else (timePassed.toFloat() / intervalMillis).coerceIn(0f, 1f)
}

@Composable
fun MyPlantCard(
    plant: Plant,
    onItemClick: () -> Unit,
//    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Миниатюра
            if (plant.imagePath != null) {
                val painter = rememberAsyncImagePainter(
                    model = File(plant.imagePath),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "Фото ${plant.name}",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Название
            Text(
                text = plant.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Полив
            Text(
                text = "💧 Полив: раз в ${plant.wateringIntervalDays} дн.",
                style = MaterialTheme.typography.bodySmall
            )

            // Прогресс-бар
            val progress = plant.getWateringProgress()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary
            )

            // Кнопка удаления (в углу)
//            IconButton(
//                onClick = { onDeleteClick() },
//                modifier = Modifier
//                    .align(Alignment.End)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Delete,
//                    contentDescription = "Удалить",
//                    tint = MaterialTheme.colorScheme.error
//                )
//            }
        }
    }
}

//@Composable
//fun PlantItem(
//    plant: Plant,
//    onItemClick: () -> Unit,
//    onDeleteClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    // 👈 Получаем контекст НА УРОВНЕ Composable
//    val context = LocalContext.current
//
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//            .clickable { onItemClick() },
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Column {
//            Text(
//                text = plant.name,
//                style = MaterialTheme.typography.titleMedium
//            )
//            Text(
//                text = "Полив: раз в ${plant.wateringIntervalDays} дн.",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//        IconButton(onClick = onDeleteClick) {
//            Icon(Icons.Default.Delete, contentDescription = "Удалить")
//        }
//
//        // ✅ Теперь context — обычная переменная, и её можно использовать в onClick
//        Button(onClick = {
//            val work = OneTimeWorkRequestBuilder<CheckWateringWorker>().build()
//            WorkManager.getInstance(context).enqueue(work) // ← context вместо LocalContext.current
//        }) {
//            Text("Запустить проверку СЕЙЧАС")
//        }
//    }
//}
//package com.example.planttracker.ui.screens
//
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Delete
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import com.example.planttracker.database.AppDatabase
//import com.example.planttracker.database.Plant
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//import androidx.compose.runtime.rememberCoroutineScope
//import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MyPlantsScreen() {
//    val context = LocalContext.current
//    val db = AppDatabase.getInstance(context)
//    val plantDao = db.plantDao()
//
//    var plants by remember { mutableStateOf<List<Plant>>(emptyList()) }
//    var showDialog by remember { mutableStateOf(false) }
//    var editingPlant by remember { mutableStateOf<Plant?>(null) }
//
//    // Получаем список растений через Flow
//    LaunchedEffect(Unit) {
//        plantDao.getAllPlants().collectLatest { list ->
//            plants = list
//        }
//    }
//
//    // Создаём CoroutineScope для launch
//    val scope = rememberCoroutineScope()
//
//    Scaffold(
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = {
//                    editingPlant = null
//                    showDialog = true
//                },
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Добавить растение")
//            }
//        }
//    ) { innerPadding ->
//        if (plants.isEmpty()) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text("Нет растений. Нажмите +, чтобы добавить первое.")
//            }
//        } else {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//            ) {
//                items(plants) { plant ->
//                    PlantItem(
//                        plant = plant,
//                        onEdit = {
//                            editingPlant = plant
//                            showDialog = true
//                        },
//                        onDelete = {
//                            // Удаление
//                            scope.launch {
//                                plantDao.delete(plant)
//                            }
//                        }
//                    )
//                }
//            }
//        }
//
//        // Диалог добавления/редактирования
//        if (showDialog) {
//            AddEditPlantDialog(
//                plant = editingPlant,
//                onConfirm = { name ->
//                    scope.launch {
//                        if (editingPlant != null) {
//                            val updated = editingPlant!!.copy(name = name)
//                            plantDao.update(updated)
//                        } else {
//                            val newPlant = Plant(name = name)
//                            plantDao.insert(newPlant)
//                        }
//                    }
//                    showDialog = false
//                },
//                onDismiss = { showDialog = false }
//            )
//        }
//    }
//}