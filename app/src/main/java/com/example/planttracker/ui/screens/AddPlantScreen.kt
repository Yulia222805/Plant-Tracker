// ui/screens/AddPlantScreen.kt
package com.example.planttracker.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.Plant
import com.example.planttracker.database.ReferencePlant
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(
    onPlantSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val plantDao = db.plantDao()
    val refPlantDao = db.referencePlantDao()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Строки для числовых полей
    var wateringIntervalText by remember { mutableStateOf("35") }
    var fertilizingIntervalText by remember { mutableStateOf("33") }

    var isFromReference by remember { mutableStateOf(false) }
    var referencePlant by remember { mutableStateOf<ReferencePlant?>(null) }

    val scope = rememberCoroutineScope()

    // Поиск растения в справочнике
    LaunchedEffect(name) {
        if (name.isNotBlank()) {
            val refPlant = refPlantDao.findByName(name)

            if (refPlant != null && !isFromReference) {
                referencePlant = refPlant
                isFromReference = true

                // Подставляем только один раз
                description = refPlant.description
                wateringIntervalText = refPlant.wateringIntervalDays.toString()
                fertilizingIntervalText = refPlant.fertilizerIntervalDays.toString()

            } else if (refPlant == null && isFromReference) {
                isFromReference = false
                referencePlant = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Добавить растение") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {

            // Название растения
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название растения") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Информация из справочника
            if (referencePlant != null) {
                item {
                    Text(
                        text = "ℹ️ Найдено в справочнике: ${referencePlant!!.name}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Описание
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание (опционально)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            // Интервал полива
            item {
                OutlinedTextField(
                    value = wateringIntervalText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            wateringIntervalText = newValue
                        }
                    },
                    label = { Text("Интервал полива (дней)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = VisualTransformation.None
                )
            }

            // Интервал удобрения
            item {
                OutlinedTextField(
                    value = fertilizingIntervalText,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            fertilizingIntervalText = newValue
                        }
                    },
                    label = { Text("Интервал удобрения (дней)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = VisualTransformation.None
                )
            }

            // Кнопка сохранения
            item {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {

                            val wateringDays = wateringIntervalText.toIntOrNull() ?: 3
                            val fertilizingDays = fertilizingIntervalText.toIntOrNull() ?: 30

                            scope.launch {
                                val newPlant = Plant(
                                    name = name,
                                    description = description.takeIf { it.isNotBlank() },
                                    wateringIntervalDays = wateringDays,
                                    lastWatered = System.currentTimeMillis(),
                                    fertilizingIntervalDays = fertilizingDays,
                                    lastFertilized = System.currentTimeMillis()
                                )
                                plantDao.insert(newPlant)
                                onPlantSaved()
                            }
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сохранить растение")
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddPlantScreen(
//    onPlantSaved: () -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val db = AppDatabase.getInstance(context)
//    val plantDao = db.plantDao()
//    val refPlantDao = db.referencePlantDao()
//
//    var name by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var wateringInterval by remember { mutableStateOf(3) }
//    var fertilizingInterval by remember { mutableStateOf(30) }
//
//    var isFromReference by remember { mutableStateOf(false) }
//    var referencePlant by remember { mutableStateOf<ReferencePlant?>(null) }
//
//    val scope = rememberCoroutineScope()
//
//    // В начале AddEditPlantScreen
////    var wateringInterval by remember { mutableStateOf(3) }
//    var wateringIntervalText by remember { mutableStateOf("3") }
//
////    var fertilizingInterval by remember { mutableStateOf(30) }
//    var fertilizingIntervalText by remember { mutableStateOf("30") }
//
//    // При изменении названия — ищем в справочнике
//    LaunchedEffect(name) {
//        if (name.isNotBlank()) {
//            val refPlant = refPlantDao.findByName(name)
//            if (refPlant != null) {
//                referencePlant = refPlant
//                isFromReference = true
//                description = refPlant.description
//                wateringInterval = refPlant.wateringIntervalDays
//                fertilizingInterval = refPlant.fertilizerIntervalDays
//            } else {
//                // Если пользователь стёр название — сбросить справочные данные
//                if (isFromReference) {
//                    isFromReference = false
//                    referencePlant = null
//                    // Не сбрасываем description и интервалы — пусть остаются, если пользователь их уже правил
//                }
//            }
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Добавить растение") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
//                    }
//                }
//            )
//        }
//    ) { innerPadding ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding),
//            verticalArrangement = Arrangement.spacedBy(16.dp),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            // Название
//            item {
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Название растения") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            // Подсказка из справочника
//            if (referencePlant != null) {
//                item {
//                    Text(
//                        text = "ℹ️ Найдено в справочнике: ${referencePlant!!.name}",
//                        color = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    )
//                }
//            }
//
//            // Описание
//            item {
//                OutlinedTextField(
//                    value = description,
//                    onValueChange = { description = it },
//                    label = { Text("Описание (опционально)") },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = false,
//                    maxLines = 3
//                )
//            }
//
//            // Интервал полива
//            item {
////                OutlinedTextField(
////                    value = wateringInterval.toString(),
////                    onValueChange = {
////                        it.toIntOrNull()?.let { days ->
////                            if (days > 0) wateringInterval = days
////                        }
////                    },
////                    label = { Text("Интервал полива (дней)") },
////                    modifier = Modifier.fillMaxWidth()
////                )
//                OutlinedTextField(
//                    value = wateringIntervalText,
//                    onValueChange = { text ->
//                        wateringIntervalText = text  // ← всегда обновляем текст
//                        text.toIntOrNull()?.takeIf { it > 0 }?.let {
//                            wateringInterval = it    // ← только валидные числа в состояние
//                        }
//                    },
//                    label = { Text("Интервал полива (дней)") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                )
//            }
//
//            // Интервал удобрения
//            item {
//                OutlinedTextField(
//                    value = fertilizingInterval.toString(),
//                    onValueChange = {
//                        it.toIntOrNull()?.let { days ->
//                            if (days > 0) fertilizingInterval = days
//                        }
//                    },
//                    label = { Text("Интервал удобрения (дней)") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            // Кнопка сохранения
//            item {
//                Button(
//                    onClick = {
//                        if (name.isNotBlank()) {
//                            scope.launch {
//                                val newPlant = Plant(
//                                    name = name,
//                                    description = if (description.isBlank()) null else description,
//                                    wateringIntervalDays = wateringInterval,
//                                    lastWatered = System.currentTimeMillis(), // ← ЯВНО указываем!
//                                    fertilizingIntervalDays = fertilizingInterval,
//                                    lastFertilized = System.currentTimeMillis() // ← и тут
//                                )
//                                plantDao.insert(newPlant)
//                                onPlantSaved()
//                            }
//                        }
//                    },
//                    enabled = name.isNotBlank(),
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Сохранить растение")
//                }
//            }
//        }
//    }
//}


//// ui/screens/AddPlantScreen.kt
//package com.example.planttracker.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.example.planttracker.database.Plant
//import com.example.planttracker.viewmodel.PlantViewModel
//import androidx.compose.material3.ExperimentalMaterial3Api
//
//
//@Composable
//fun AddPlantScreen() {
//    // Обёртка без параметров — для NavHost
//    AddPlantScreenContent(onPlantSaved = {})
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddPlantScreenContent(
//    onPlantSaved: () -> Unit
//) {
//    var name by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var wateringInterval by remember { mutableStateOf("7") }
//    var fertilizingInterval by remember { mutableStateOf("30") }
//
//    val viewModel: PlantViewModel = hiltViewModel()
//
//    Scaffold(topBar = { TopAppBar(title = { Text("Добавить растение") }) }) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название *") })
//            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Описание") })
//            OutlinedTextField(value = wateringInterval, onValueChange = { wateringInterval = it }, label = { Text("Полив (дни)") })
//            OutlinedTextField(value = fertilizingInterval, onValueChange = { fertilizingInterval = it }, label = { Text("Удобрение (дни)") })
//
//            Button(
//                onClick = {
//                    if (name.isNotBlank()) {
//                        val plant = Plant(
//                            name = name.trim(),
//                            description = description.ifBlank { null },
//                            wateringIntervalDays = wateringInterval.toIntOrNull() ?: 7,
//                            fertilizingIntervalDays = fertilizingInterval.toIntOrNull() ?: 30,
//                            // 👇 Добавь эти два параметра!
//                            lastWatered = System.currentTimeMillis(), // текущая дата
//                            imagePath = null // пока нет фото
//                        )
//                        viewModel.addPlant(plant)
//                        onPlantSaved()
//                    }
//                },
//                enabled = name.isNotBlank(),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Сохранить")
//            }
//        }
//    }
//}