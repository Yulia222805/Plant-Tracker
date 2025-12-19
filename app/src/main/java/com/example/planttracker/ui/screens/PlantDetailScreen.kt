package com.example.planttracker.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.Plant
import com.example.planttracker.ui.components.AnimatedWateringProgress
import com.example.planttracker.ui.components.WateringProgressWithLottie
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PlantDetailScreen(
//    plantId: Long,
//    onBack: () -> Unit,
//    onEdit: () -> Unit
//) {
//    val context = LocalContext.current
//    val db = AppDatabase.getInstance(context)
//    val plantDao = db.plantDao()
//
//    var plant by remember { mutableStateOf<Plant?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    // Загружаем растение из базы данных
//    LaunchedEffect(plantId) {
//        plant = plantDao.getPlantById(plantId)
//        isLoading = false
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(plant?.name ?: "Растение") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Назад"
//                        )
//                    }
//                },
//                actions = {
//                    if (plant != null) {
//                        IconButton(onClick = onEdit) {
//                            Icon(
//                                imageVector = Icons.Filled.Edit,
//                                contentDescription = "Редактировать"
//                            )
//                        }
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        if (isLoading) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        } else if (plant != null) {
//            val currentPlant = plant!!
//
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding),
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                contentPadding = PaddingValues(16.dp)
//            ) {
//                // Фото растения
//                item {
//                    if (currentPlant.imagePath != null) {
//                        val painter = rememberAsyncImagePainter(
//                            model = Uri.parse(currentPlant.imagePath),
//                            contentScale = ContentScale.Crop
//                        )
//                        Image(
//                            painter = painter,
//                            contentDescription = "Фото ${currentPlant.name}",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(250.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                        )
//                    } else {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(250.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(MaterialTheme.colorScheme.surfaceVariant),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.AddPhotoAlternate,
//                                contentDescription = "Нет фото",
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//                }
//
//                // Название
//                item {
//                    Text(
//                        text = currentPlant.name,
//                        style = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//
//                // Описание
//                item {
//                    Text(
//                        text = "Описание",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(
//                        text = currentPlant.description ?: "Описание отсутствует",
//                        color = if (currentPlant.description.isNullOrBlank()) {
//                            MaterialTheme.colorScheme.onSurfaceVariant
//                        } else {
//                            MaterialTheme.colorScheme.onSurface
//                        }
//                    )
//                }
//
//                // Последний полив
//                item {
//                    Text(
//                        text = "Последний полив",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = formatDate(currentPlant.lastWatered))
//                }
//
//                // Следующий полив
//                item {
//                    val nextWatering = currentPlant.lastWatered +
//                            currentPlant.wateringIntervalDays * 24L * 60L * 60L * 1000L
//                    Text(
//                        text = "Следующий полив",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = formatDate(nextWatering))
//                }
//
//                // Интервал полива
//                item {
//                    Text(
//                        text = "Интервал полива",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = "${currentPlant.wateringIntervalDays} дней")
//                }
//
//                // Последнее удобрение
//                item {
//                    Text(
//                        text = "Последнее удобрение",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = formatDate(currentPlant.lastFertilized))
//                }
//
//                // Интервал удобрения
//                item {
//                    Text(
//                        text = "Интервал удобрения",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = "${currentPlant.fertilizingIntervalDays} дней")
//                }
//            }
//        }// ← ЗАКРЫВАЮЩАЯ СКОБКА LazyColumn
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val plantDao = db.plantDao()

    var plant by remember { mutableStateOf<Plant?>(null) }

    var showFullScreenImage by remember { mutableStateOf(false) }

    LaunchedEffect(plantId) {
        plant = plantDao.getPlantById(plantId)
    }

    val sdf = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    plant?.let { p ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(p.name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Редактировать"
                            )
                        }

                        // Кнопка удаления
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                if (p.imagePath != null) {
                    item {
                        val painter = rememberAsyncImagePainter(
                            model = File(p.imagePath),
                            contentScale = ContentScale.Crop
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showFullScreenImage = true
                                }
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant,
                                    RoundedCornerShape(12.dp)
                                )
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = "Фото растения",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                item { DetailRow(label = "Полив", value = "раз в ${p.wateringIntervalDays} дн.") }
                item { DetailRow(label = "Последний полив", value = sdf.format(Date(p.lastWatered))) }
                item { DetailRow(label = "Удобрение", value = "раз в ${p.fertilizingIntervalDays} дн.") }
                item { DetailRow(label = "Последнее удобрение", value = sdf.format(Date(p.lastFertilized))) }
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Прогресс до следующего полива:")

                        // Прогресс + анимация в одной строке
                        WateringProgressWithLottie(
                            progress = p.getWateringProgress(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }
                p.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    item {
                        Text(
                            text = "Описание:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    item {
                        Text(text = desc, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        val currentPlant = plant

        if (showDeleteDialog && currentPlant != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Удалить растение?") },
                text = { Text("Вы уверены, что хотите удалить «${currentPlant.name}»?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                plantDao.delete(currentPlant)
                                onBack() // возврат назад после удаления
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }



        // ✅ ПОЛНОЭКРАННЫЙ ПРОСМОТР — ПОВЕРХ ВСЕГО!
        if (showFullScreenImage && p.imagePath != null) {
            FullScreenImageViewer(
                imageUrl = p.imagePath,
                onDismiss = { showFullScreenImage = false }
            )
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Растение не найдено")
        }
    }
}

// Вспомогательный composable — вынесен для переиспользования
@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PlantDetailScreen(
//    plantId: Long,
//    onBack: () -> Unit,
//    onEdit: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val context = LocalContext.current
//    val db = AppDatabase.getInstance(context)
//    val plantDao = db.plantDao()
//
//    var plant by remember { mutableStateOf<Plant?>(null) }
//
//    LaunchedEffect(plantId) {
//        plant = plantDao.getPlantById(plantId)
//    }
//
//    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
//
//    plant?.let { p ->
//        Scaffold(
//            topBar = {
//                TopAppBar(
//                    title = { Text(p.name) },
//                    navigationIcon = {
//                        IconButton(onClick = onBack) {
//                            Icon(
//                                imageVector = Icons.Default.ArrowBack,
//                                contentDescription = "Назад"
//                            )
//                        }
//                    },
//                    actions = {
//                        IconButton(onClick = onEdit) {
//                            Icon(
//                                imageVector = Icons.Default.Edit,
//                                contentDescription = "Редактировать"
//                            )
//                        }
//                    }
//                )
//            }
//        ) { innerPadding ->
//            LazyColumn(
//                modifier = modifier
//                    .fillMaxSize()
//                    .padding(innerPadding),
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                contentPadding = PaddingValues(16.dp)
//            ) {
//                // Фото
//                item {
//                    p.imagePath?.let { path ->
//                        val painter = rememberAsyncImagePainter(
//                            model = File(path), // ✅
//                            contentScale = ContentScale.Crop
//                        )
//                        Image(
//                            painter = painter,
//                            contentDescription = "Фото растения",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(250.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                        )
//                    }
//                }
//
//                // Поля
//                item { Text("Интервал полива: ${p.wateringIntervalDays} дней") }
//                item { Text("Последний полив: ${sdf.format(Date(p.lastWatered))}") }
//                item { Text("Интервал удобрения: ${p.fertilizingIntervalDays} дней") }
//                item { Text("Последнее удобрение: ${sdf.format(Date(p.lastFertilized))}") }
//
//                p.description?.takeIf { it.isNotBlank() }?.let { desc ->
//                    item {
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            text = "Описание:",
//                            style = MaterialTheme.typography.titleMedium
//                        )
//                        Text(text = desc, style = MaterialTheme.typography.bodyMedium)
//                    }
//                }
//            }
//        }
//    } ?: run {
//        // Если растение не найдено
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Text("Растение не найдено")
//        }
//    }
//}
//
//// Вспомогательная функция для форматирования даты
//@Composable
//fun formatDate(timestamp: Long): String {
//    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
//}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PlantDetailScreen(
//    plantId: Long,
//    onBack: () -> Unit,
//    onEdit: () -> Unit
//) {
//    val context = LocalContext.current
//    val db = AppDatabase.getInstance(context)
//    val plantDao = db.plantDao()
//
//    var plant by remember { mutableStateOf<Plant?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    LaunchedEffect(plantId) {
//        plant = plantDao.getPlantById(plantId)
//        isLoading = false
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(plant?.name ?: "Растение") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(
//                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = "Назад"
//                        )
//                    }
//                },
//                actions = {
//                    if (plant != null) {
//                        IconButton(onClick = onEdit) {
//                            Icon(
//                                imageVector = Icons.Filled.Edit,
//                                contentDescription = "Редактировать"
//                            )
//                        }
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        if (isLoading) {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        } else if (plant != null) {
//            val currentPlant = plant!!
//
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding),
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                contentPadding = PaddingValues(16.dp)
//            ) {
//                // 💚 ФОТО РАСТЕНИЯ (новое поле, но в начале)
//                item {
//                    if (currentPlant.imagePath != null) {
//                        val painter = rememberAsyncImagePainter(
//                            model = Uri.parse(currentPlant.imagePath),
//                            contentScale = ContentScale.Crop
//                        )
//                        Image(
//                            painter = painter,
//                            contentDescription = "Фото ${currentPlant.name}",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(250.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                        )
//                    } else {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(250.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(MaterialTheme.colorScheme.surfaceVariant),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.AddPhotoAlternate,
//                                contentDescription = "Нет фото",
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                        }
//                    }
//                }
//
//                // 📝 ОСТАЛЬНЫЕ ПОЛЯ — как раньше
//                item {
//                    Text(
//                        text = "Название",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = currentPlant.name)
//                }
//
//                item {
//                    Text(
//                        text = "Описание",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(
//                        text = currentPlant.description ?: "Описание отсутствует",
//                        color = if (currentPlant.description.isNullOrBlank()) {
//                            MaterialTheme.colorScheme.onSurfaceVariant
//                        } else {
//                            MaterialTheme.colorScheme.onSurface
//                        }
//                    )
//                }
//
//                item {
//                    Text(
//                        text = "Последний полив",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = formatDate(currentPlant.lastWatered))
//                }
//
//                item {
//                    val nextWatering = currentPlant.lastWatered +
//                            currentPlant.wateringIntervalDays * 24L * 60L * 60L * 1000L
//                    Text(
//                        text = "Следующий полив",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = formatDate(nextWatering))
//                }
//
//                item {
//                    Text(
//                        text = "Интервал полива",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = "${currentPlant.wateringIntervalDays} дней")
//                }
//
//                item {
//                    Text(
//                        text = "Последнее удобрение",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = formatDate(currentPlant.lastFertilized))
//                }
//
//                item {
//                    Text(
//                        text = "Интервал удобрения",
//                        style = MaterialTheme.typography.titleMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                    Text(text = "${currentPlant.fertilizingIntervalDays} дней")
//                }
//            }
//        } else {
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "Растение не найдено",
//                    color = MaterialTheme.colorScheme.error
//                )
//            }
//        }
//    }
//}
//
//// Вспомогательная функция — только один раз!
//@Composable
//fun formatDate(timestamp: Long): String {
//    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
//}