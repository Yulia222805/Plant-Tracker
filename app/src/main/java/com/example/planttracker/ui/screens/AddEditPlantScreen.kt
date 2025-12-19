// ui/screens/AddEditPlantScreen.kt
package com.example.planttracker.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.Plant
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material.icons.filled.CalendarToday
import com.example.planttracker.database.ReferencePlant
import androidx.compose.ui.text.input.KeyboardType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.planttracker.workers.CheckWateringWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
//import java.util.jar.Manifest
import com.example.planttracker.R
//import androidx.compose.ui.res.stringResource

import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlantScreen(
    plantId: Long? = null,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val plantDao = db.plantDao()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var wateringIntervalText by remember { mutableStateOf("3") }
    var fertilizingIntervalText by remember { mutableStateOf("30") }

    var lastWateredMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var lastFertilizedMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showReferenceDialog by remember { mutableStateOf(false) }
    var showWateringDatePicker by remember { mutableStateOf(false) }
    var showFertilizingDatePicker by remember { mutableStateOf(false) }
    var imagePath by remember { mutableStateOf<String?>(null) }

    var photoFile by remember { mutableStateOf<File?>(null) }
    var showPhotoSourceDialog by remember { mutableStateOf(false) }

    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val activity = context as? ComponentActivity

    // Валидация интервалов
    val isValidInterval: (String) -> Boolean = { text ->
        text.toIntOrNull()?.let { it > 0 } == true
    }
    val isNameValid by remember(name) { mutableStateOf(name.isNotBlank()) }
    val isWateringValid by remember(wateringIntervalText) {
        mutableStateOf(isValidInterval(wateringIntervalText))
    }
    val isFertilizingValid by remember(fertilizingIntervalText) {
        mutableStateOf(isValidInterval(fertilizingIntervalText))
    }

    val isFormValid by remember(isNameValid, isWateringValid, isFertilizingValid) {
        mutableStateOf(isNameValid && isWateringValid && isFertilizingValid)
    }

    var hasAttemptedSave by remember { mutableStateOf(false) }

    // Загрузка данных при редактировании
    LaunchedEffect(plantId) {
        if (plantId != null && plantId != 0L) {
            val plant = plantDao.getPlantById(plantId)
            if (plant != null) {
                name = plant.name
                description = plant.description ?: ""
                wateringIntervalText = plant.wateringIntervalDays.toString()
                fertilizingIntervalText = plant.fertilizingIntervalDays.toString()
                lastWateredMillis = plant.lastWatered
                lastFertilizedMillis = plant.lastFertilized
                imagePath = plant.imagePath
            }
        }
    }


    fun saveImageFromUri(context: Context, uri: Uri): String? {
        return try {
            val fileName = "plant_${System.currentTimeMillis()}.jpg"
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
            FileOutputStream(outputFile).use { out ->
                inputStream?.copyTo(out)
            }
            inputStream?.close()
            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Launcher для галереи
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = saveImageFromUri(activity as Context, it)
            if (savedPath != null) {
                imagePath = savedPath
            }
        }
    }

    // Launcher для камеры
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Фото уже сохранено в photoFile
            imagePath = photoFile?.absolutePath
        } else {
            // Пользователь отменил съёмку
            photoFile = null
        }
    }

    fun createImageFileUri(context: Context): Uri {
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw IllegalStateException("Не удалось получить папку Pictures")

        if (!picturesDir.exists()) picturesDir.mkdirs()

        val file = File(picturesDir, "plant_${System.currentTimeMillis()}.jpg")
        photoFile = file // Сохраняешь для последующего доступа

        return FileProvider.getUriForFile(context, "com.example.planttracker.fileprovider", file)
    }
    // Функция создания временного файла
    fun createImageFile(context: Context): File {
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (picturesDir == null) {
            throw IllegalStateException("Не удалось получить директорию Pictures")
        }

        // Убедись, что папка существует
        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
        }

        val fileName = "plant_${System.currentTimeMillis()}.jpg"
        return File(picturesDir, fileName).apply { photoFile = this }
    }

    // Launcher для запроса разрешения на камеру
//    val requestPermissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted && activity != null) {
////            photoFile = createImageFile(activity)
////            cameraLauncher.launch(photoFile?.let { Uri.fromFile(it) })
//            val photoUri = createImageFileUri(context) // ← уже возвращает content:// uri
//            cameraLauncher.launch(photoUri)
//        } else {
//            Toast.makeText(context, R.string.toast_camera_permission_needed, Toast.LENGTH_SHORT).show()
//        }
//    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && activity != null) {
            try {
                val photoUri = createImageFileUri(context) // ← content:// uri
                cameraLauncher.launch(photoUri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, R.string.toast_camera_permission_needed, Toast.LENGTH_SHORT).show()
        }
    }


    // Функция открытия камеры с запросом разрешения
    val openCameraWithPermission = {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val photoUri = createImageFileUri(context)
                cameraLauncher.launch(photoUri)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Ошибка создания файла: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (plantId == null) stringResource(R.string.screen_add_plant_title) else stringResource(R.string.screen_edit_plant_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.plant_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = hasAttemptedSave && name.isBlank(),
                    supportingText = {
                        if (hasAttemptedSave && name.isBlank()) {
                            Text(stringResource(R.string.error_required_field), color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.plant_description_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }

            item {
                OutlinedTextField(
                    value = wateringIntervalText,
                    onValueChange = { wateringIntervalText = it },
                    label = { Text(stringResource(R.string.watering_interval_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = hasAttemptedSave && wateringIntervalText.isNotBlank() && !isValidInterval(wateringIntervalText),
                    supportingText = {
                        if (hasAttemptedSave && wateringIntervalText.isNotBlank() && !isValidInterval(wateringIntervalText)) {
                            Text(stringResource(R.string.error_positive_number), color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = sdf.format(Date(lastWateredMillis)),
                    onValueChange = { /* read-only */ },
                    label = { Text(stringResource(R.string.last_watered_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showWateringDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
                        }
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = fertilizingIntervalText,
                    onValueChange = { fertilizingIntervalText = it },
                    label = { Text(stringResource(R.string.fertilizing_interval_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = hasAttemptedSave && fertilizingIntervalText.isNotBlank() && !isValidInterval(fertilizingIntervalText),
                    supportingText = {
                        if (hasAttemptedSave && fertilizingIntervalText.isNotBlank() && !isValidInterval(fertilizingIntervalText)) {
                            Text(stringResource(R.string.error_positive_number), color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = sdf.format(Date(lastFertilizedMillis)),
                    onValueChange = { /* read-only */ },
                    label = { Text(stringResource(R.string.last_fertilized_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showFertilizingDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
                        }
                    }
                )
            }

            item {
                Button(
                    onClick = { showReferenceDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_fill_from_reference))
                }
            }

            // Кнопка добавления фото
            item {
                OutlinedButton(
                    onClick = { showPhotoSourceDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_add_plant_photo))
                }
            }

            // Превью фото
            item {
                imagePath?.let { path ->
                    val painter = rememberAsyncImagePainter(
                        model = File(path), // ✅
                        contentScale = ContentScale.Crop
                    )
                    Image(
                        painter = painter,
                        contentDescription = stringResource(R.string.cd_plant_photo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            // Кнопка сохранения
            item {
                Button(
                    onClick = {
                        hasAttemptedSave = true // ← УСТАНАВЛИВАЕМ ФЛАГ ПРИ НАЖАТИИ
                        if (isFormValid) {
                            val wateringDays = wateringIntervalText.toInt()
                            val fertilizingDays = fertilizingIntervalText.toInt()

                            scope.launch {
                                if (plantId == null) {
                                    val newPlant = Plant(
                                        name = name,
                                        description = description.takeIf { it.isNotBlank() },
                                        wateringIntervalDays = wateringDays,
                                        lastWatered = lastWateredMillis,
                                        fertilizingIntervalDays = fertilizingDays,
                                        lastFertilized = lastFertilizedMillis,
                                        imagePath = imagePath
                                    )
                                    plantDao.insert(newPlant)
                                } else {
                                    val existingPlant = plantDao.getPlantById(plantId)
                                    if (existingPlant != null) {
                                        val updatedPlant = existingPlant.copy(
                                            name = name,
                                            description = description.takeIf { it.isNotBlank() },
                                            wateringIntervalDays = wateringDays,
                                            lastWatered = lastWateredMillis,
                                            fertilizingIntervalDays = fertilizingDays,
                                            lastFertilized = lastFertilizedMillis,
                                            imagePath = imagePath
                                        )
                                        plantDao.update(updatedPlant)
                                    }
                                }

                                WorkManager.getInstance(context)
                                    .enqueue(OneTimeWorkRequestBuilder<CheckWateringWorker>().build())

                                onSaved()
                            }
                        }
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (plantId == null) stringResource(R.string.btn_create_plant) else stringResource(R.string.btn_save_changes))
                }
//                Button(
//                    onClick = {
//                        if (isFormValid) {
//                            val wateringDays = wateringIntervalText.toInt()
//                            val fertilizingDays = fertilizingIntervalText.toInt()
//
//                            scope.launch {
//                                if (plantId == null) {
//                                    val newPlant = Plant(
//                                        name = name,
//                                        description = description.takeIf { it.isNotBlank() },
//                                        wateringIntervalDays = wateringDays,
//                                        lastWatered = lastWateredMillis,
//                                        fertilizingIntervalDays = fertilizingDays,
//                                        lastFertilized = lastFertilizedMillis,
//                                        imagePath = imagePath
//                                    )
//                                    plantDao.insert(newPlant)
//                                } else {
//                                    val existingPlant = plantDao.getPlantById(plantId)
//                                    if (existingPlant != null) {
//                                        val updatedPlant = existingPlant.copy(
//                                            name = name,
//                                            description = description.takeIf { it.isNotBlank() },
//                                            wateringIntervalDays = wateringDays,
//                                            lastWatered = lastWateredMillis,
//                                            fertilizingIntervalDays = fertilizingDays,
//                                            lastFertilized = lastFertilizedMillis,
//                                            imagePath = imagePath
//                                        )
//                                        plantDao.update(updatedPlant)
//                                    }
//                                }
//
//                                // Запускаем проверку уведомлений
//                                WorkManager.getInstance(context)
//                                    .enqueue(OneTimeWorkRequestBuilder<CheckWateringWorker>().build())
//
//                                onSaved()
//                            }
//                        }
//                    },
//                    enabled = isFormValid,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(if (plantId == null) "Создать растение" else "Сохранить изменения")
//                }
            }
        }
    }

    // DatePickers
    if (showWateringDatePicker) {
        val state = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = lastWateredMillis
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showWateringDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { lastWateredMillis = it }
                        showWateringDatePicker = false
                    }
                ) {
                    androidx.compose.material3.Text("OK")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = state)
        }
    }

    if (showFertilizingDatePicker) {
        val state = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = lastFertilizedMillis
        )
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showFertilizingDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { lastFertilizedMillis = it }
                        showFertilizingDatePicker = false
                    }
                ) {
                    androidx.compose.material3.Text("OK")
                }
            }
        ) {
            androidx.compose.material3.DatePicker(state = state)
        }
    }

    // Диалог выбора источника фото
    if (showPhotoSourceDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPhotoSourceDialog = false },
            title = { androidx.compose.material3.Text(stringResource(R.string.dialog_photo_source_title)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        imagePickerLauncher.launch("image/*")
                    }
                ) {
                    androidx.compose.material3.Text(stringResource(R.string.dialog_photo_source_gallery))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showPhotoSourceDialog = false
                        openCameraWithPermission()
                    }
                ) {
                    androidx.compose.material3.Text(stringResource(R.string.dialog_photo_source_camera))
                }
            }
        )
    }

    // Справочник
    if (showReferenceDialog) {
        ReferenceSearchDialog(
            onDismiss = { showReferenceDialog = false },
            onPlantSelected = { refPlant ->
                name = refPlant.name
                description = refPlant.description
                wateringIntervalText = refPlant.wateringIntervalDays.toString()
                fertilizingIntervalText = refPlant.fertilizerIntervalDays.toString()
                showReferenceDialog = false
            }
        )
    }
}

//рабочий, с уведомлениями, но без камеры
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddEditPlantScreen(
//    plantId: Long? = null,
//    onSaved: () -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val db = AppDatabase.getInstance(context)
//    val plantDao = db.plantDao()
//
//    var name by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var wateringIntervalText by remember { mutableStateOf("3") }
//    var fertilizingIntervalText by remember { mutableStateOf("30") }
//
//    var lastWateredMillis by remember { mutableStateOf(System.currentTimeMillis()) }
//    var lastFertilizedMillis by remember { mutableStateOf(System.currentTimeMillis()) }
//    var showReferenceDialog by remember { mutableStateOf(false) }
//    var showWateringDatePicker by remember { mutableStateOf(false) }
//    var showFertilizingDatePicker by remember { mutableStateOf(false) }
//    var imagePath by remember { mutableStateOf<String?>(null) }
//
//    var photoFile by remember { mutableStateOf<File?>(null) }
//    var showPhotoSourceDialog by remember { mutableStateOf(false) }
//
//    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
//
//    // Валидация
//    val isValidInterval: (String) -> Boolean = { text ->
//        text.toIntOrNull()?.let { it > 0 } == true
//    }
//    val isFormValid = remember(name, wateringIntervalText, fertilizingIntervalText) {
//        name.isNotBlank() &&
//                isValidInterval(wateringIntervalText) &&
//                isValidInterval(fertilizingIntervalText) // ← исправлено
//    }
//
//    // Загрузка при редактировании
//    LaunchedEffect(plantId) {
//        if (plantId != null && plantId != 0L) {
//            val plant = plantDao.getPlantById(plantId)
//            if (plant != null) {
//                name = plant.name
//                description = plant.description ?: ""
//                wateringIntervalText = plant.wateringIntervalDays.toString()
//                fertilizingIntervalText = plant.fertilizingIntervalDays.toString()
//                lastWateredMillis = plant.lastWatered
//                lastFertilizedMillis = plant.lastFertilized
//                imagePath = plant.imagePath
//            }
//        }
//    }
//
//    // Launchers
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { imagePath = it.toString() }
//    }
//
//    val cameraLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.TakePicture()
//    ) { success ->
//        if (success && photoFile != null) {
//            imagePath = Uri.fromFile(photoFile!!).toString()
//        }
//    }
//
//    fun createImageFile(context: Context): File {
//        return File.createTempFile(
//            "plant_${System.currentTimeMillis()}_",
//            ".jpg",
//            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        ).apply { photoFile = this }
//    }
//
//    val activity = LocalContext.current as? ComponentActivity
//
//    val requestPermissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted && activity != null) {
//            photoFile = createImageFile(activity)
//            cameraLauncher.launch(photoFile?.let { Uri.fromFile(it) })
//        } else {
//            Toast.makeText(activity, "Нужен доступ к камере", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//
//    val openCameraWithPermission = {
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//            photoFile = createImageFile(context)
//            cameraLauncher.launch(photoFile?.let { Uri.fromFile(it) })
//        } else {
//            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//        }
//    }
//
//    val scope = rememberCoroutineScope()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (plantId == null) "Добавить растение" else "Редактировать растение") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
//            item {
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Название растения") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = name.isBlank(),
//                    supportingText = {
//                        if (name.isBlank()) {
//                            Text("Обязательное поле", color = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = description,
//                    onValueChange = { description = it },
//                    label = { Text(stringResource(R.string.plant_description_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    maxLines = 3
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = wateringIntervalText,
//                    onValueChange = { wateringIntervalText = it },
//                    label = { Text(stringResource(R.string.watering_interval_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    isError = wateringIntervalText.isNotBlank() && !isValidInterval(wateringIntervalText),
//                    supportingText = {
//                        if (wateringIntervalText.isNotBlank() && !isValidInterval(wateringIntervalText)) {
//                            Text("Введите число больше 0", color = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = sdf.format(Date(lastWateredMillis)),
//                    onValueChange = { /* read-only */ },
//                    label = { Text(stringResource(R.string.last_watered_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    readOnly = true,
//                    trailingIcon = {
//                        IconButton(onClick = { showWateringDatePicker = true }) {
//                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = fertilizingIntervalText,
//                    onValueChange = { fertilizingIntervalText = it },
//                    label = { Text("Интервал удобрения (дней)") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    isError = fertilizingIntervalText.isNotBlank() && !isValidInterval(fertilizingIntervalText),
//                    supportingText = {
//                        if (fertilizingIntervalText.isNotBlank() && !isValidInterval(fertilizingIntervalText)) {
//                            Text("Введите число больше 0", color = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = sdf.format(Date(lastFertilizedMillis)),
//                    onValueChange = { /* read-only */ },
//                    label = { Text("Последнее удобрение") },
//                    modifier = Modifier.fillMaxWidth(),
//                    readOnly = true,
//                    trailingIcon = {
//                        IconButton(onClick = { showFertilizingDatePicker = true }) {
//                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
//                        }
//                    }
//                )
//            }
//
//            item {
//                Button(
//                    onClick = { showReferenceDialog = true },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Заполнить из справочника")
//                }
//            }
//
//            // Кнопка добавления фото
//            item {
//                OutlinedButton(
//                    onClick = { showPhotoSourceDialog = true },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Добавить фото растения")
//                }
//            }
//
//            // Превью фото
//            item {
//                imagePath?.let { uriString ->
//                    val painter = rememberAsyncImagePainter(
//                        model = Uri.parse(uriString),
//                        contentScale = ContentScale.Crop
//                    )
//                    Image(
//                        painter = painter,
//                        contentDescription = "Фото растения",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                    )
//                }
//            }
//
//            // Кнопка сохранения
//            item {
//                Button(
//                    onClick = {
//                        if (isFormValid) {
//                            val wateringDays = wateringIntervalText.toInt()
//                            val fertilizingDays = fertilizingIntervalText.toInt()
//
//                            scope.launch {
//                                if (plantId == null) {
//                                    val newPlant = Plant(
//                                        name = name,
//                                        description = description.takeIf { it.isNotBlank() },
//                                        wateringIntervalDays = wateringDays,
//                                        lastWatered = lastWateredMillis,
//                                        fertilizingIntervalDays = fertilizingDays,
//                                        lastFertilized = lastFertilizedMillis,
//                                        imagePath = imagePath
//                                    )
//                                    plantDao.insert(newPlant)
//                                } else {
//                                    val existingPlant = plantDao.getPlantById(plantId)
//                                    if (existingPlant != null) {
//                                        val updatedPlant = existingPlant.copy(
//                                            name = name,
//                                            description = description.takeIf { it.isNotBlank() },
//                                            wateringIntervalDays = wateringDays,
//                                            lastWatered = lastWateredMillis,
//                                            fertilizingIntervalDays = fertilizingDays,
//                                            lastFertilized = lastFertilizedMillis,
//                                            imagePath = imagePath
//                                        )
//                                        plantDao.update(updatedPlant)
//                                    }
//                                }
//
//                                // 👇 ЗАПУСКАЕМ ПРОВЕРКУ УВЕДОМЛЕНИЙ СРАЗУ!
//                                WorkManager.getInstance(context)
//                                    .enqueue(OneTimeWorkRequestBuilder<CheckWateringWorker>().build())
//
//                                onSaved()
//                            }
//                        }
//                    },
//                    enabled = isFormValid,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(if (plantId == null) "Создать растение" else "Сохранить изменения")
//                }
//            }
//        }
//    }
//
//    // DatePickers
//    if (showWateringDatePicker) {
//        val state = rememberDatePickerState(initialSelectedDateMillis = lastWateredMillis)
//        DatePickerDialog(
//            onDismissRequest = { showWateringDatePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        state.selectedDateMillis?.let { lastWateredMillis = it }
//                        showWateringDatePicker = false
//                    }
//                ) { Text("OK") }
//            }
//        ) { DatePicker(state) }
//    }
//
//    if (showFertilizingDatePicker) {
//        val state = rememberDatePickerState(initialSelectedDateMillis = lastFertilizedMillis)
//        DatePickerDialog(
//            onDismissRequest = { showFertilizingDatePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        state.selectedDateMillis?.let { lastFertilizedMillis = it }
//                        showFertilizingDatePicker = false
//                    }
//                ) { Text("OK") }
//            }
//        ) { DatePicker(state) }
//    }
//
//    // Диалог выбора фото
//    if (showPhotoSourceDialog) {
//        AlertDialog(
//            onDismissRequest = { showPhotoSourceDialog = false },
//            title = { Text("Выберите источник") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        showPhotoSourceDialog = false
//                        imagePickerLauncher.launch("image/*")
//                    }
//                ) { Text("Галерея") }
//            },
//            dismissButton = {
//                TextButton(
//                    onClick = {
//                        showPhotoSourceDialog = false
//                        openCameraWithPermission()
//                    }
//                ) { Text("Камера") }
//            }
//        )
//    }
//
//    // Справочник
//    if (showReferenceDialog) {
//        ReferenceSearchDialog(
//            onDismiss = { showReferenceDialog = false },
//            onPlantSelected = { refPlant ->
//                name = refPlant.name
//                description = refPlant.description
//                wateringIntervalText = refPlant.wateringIntervalDays.toString()
//                fertilizingIntervalText = refPlant.fertilizerIntervalDays.toString()
//                showReferenceDialog = false
//            }
//        )
//    }
//}

//рабочий, но без уведомлений и картинок
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddEditPlantScreen(
//    plantId: Long? = null,
//    onSaved: () -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val db = AppDatabase.getInstance(context)
//    val plantDao = db.plantDao()
//
//    var name by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var wateringIntervalText by remember { mutableStateOf("3") }
//    var fertilizingIntervalText by remember { mutableStateOf("30") }
//
//    var lastWateredMillis by remember { mutableStateOf(System.currentTimeMillis()) }
//    var lastFertilizedMillis by remember { mutableStateOf(System.currentTimeMillis()) }
//    var showReferenceDialog by remember { mutableStateOf(false) }
//    var showWateringDatePicker by remember { mutableStateOf(false) }
//    var showFertilizingDatePicker by remember { mutableStateOf(false) }
//
//    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
//
//    var imagePath by remember { mutableStateOf<String?>(null) }
//
//    // Валидация: число должно быть положительным
//    val isValidInterval: (String) -> Boolean = { text ->
//        text.toIntOrNull()?.let { it > 0 } == true
//    }
//
//    // Полная валидация формы
//    val isFormValid = remember(name, wateringIntervalText, fertilizingIntervalText) {
//        name.isNotBlank() &&
//                isValidInterval(wateringIntervalText) &&
//                isValidInterval(fertilizingIntervalText)
//    }
//
//
//
//    // Загрузка данных при редактировании
//    LaunchedEffect(plantId) {
//        if (plantId != null && plantId != 0L) {
//            val plant = plantDao.getPlantById(plantId)
//            if (plant != null) {
//                name = plant.name
//                description = plant.description ?: ""
//                wateringIntervalText = plant.wateringIntervalDays.toString()
//                fertilizingIntervalText = plant.fertilizingIntervalDays.toString()
//                lastWateredMillis = plant.lastWatered
//                lastFertilizedMillis = plant.lastFertilized
//                imagePath = plant.imagePath
//            }
//        }
//    }
//
////    val imagePickerLauncher = rememberLauncherForActivityResult(
////        ActivityResultContracts.GetContent()
////    ) { uri: Uri? ->
////        uri?.let {
////            imagePath = it.toString() // сохраняем URI как строку
////        }
////    }
//
//    var photoFile by remember { mutableStateOf<File?>(null) }
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { imagePath = it.toString() }
//    }
//    val cameraLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.TakePicture()
//    ) { success ->
//        if (success && photoFile != null) {
//            imagePath = Uri.fromFile(photoFile!!).toString()
//        }
//    }
//
//    fun createImageFile(): File {
//        return File.createTempFile(
//            "plant_${System.currentTimeMillis()}_",
//            ".jpg",
//            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        ).apply { photoFile = this }
//    }
//    var showPhotoSourceDialog by remember { mutableStateOf(false) }
//
//    val requestPermissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            photoFile = createImageFile()
//            cameraLauncher.launch(photoFile?.let { Uri.fromFile(it) })
//        } else {
//            Toast.makeText(context, "Нужен доступ к камере", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//// Функция открытия камеры
//    val openCamera = {
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
//            == PackageManager.PERMISSION_GRANTED) {
//            photoFile = createImageFile()
//            cameraLauncher.launch(photoFile?.let { Uri.fromFile(it) })
//        } else {
//            // 👇 СИСТЕМНЫЙ ЗАПРОС РАЗРЕШЕНИЯ
//            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//        }
//    }
//
//    val scope = rememberCoroutineScope()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (plantId == null) "Добавить растение" else "Редактировать растение") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
//            item {
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Название растения") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = name.isBlank(),
//                    supportingText = {
//                        if (name.isBlank()) {
//                            Text("Обязательное поле", color = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = description,
//                    onValueChange = { description = it },
//                    label = { Text(stringResource(R.string.plant_description_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = false,
//                    maxLines = 3
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = wateringIntervalText,
//                    onValueChange = { wateringIntervalText = it },
//                    label = { Text(stringResource(R.string.watering_interval_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    isError = wateringIntervalText.isNotBlank() && !isValidInterval(wateringIntervalText),
//                    supportingText = {
//                        if (wateringIntervalText.isNotBlank() && !isValidInterval(wateringIntervalText)) {
//                            Text("Введите число больше 0", color = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = sdf.format(Date(lastWateredMillis)),
//                    onValueChange = { /* only read */ },
//                    label = { Text(stringResource(R.string.last_watered_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    readOnly = true,
//                    trailingIcon = {
//                        IconButton(onClick = { showWateringDatePicker = true }) {
//                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = fertilizingIntervalText,
//                    onValueChange = { fertilizingIntervalText = it },
//                    label = { Text("Интервал удобрения (дней)") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    isError = fertilizingIntervalText.isNotBlank() && !isValidInterval(fertilizingIntervalText),
//                    supportingText = {
//                        if (fertilizingIntervalText.isNotBlank() && !isValidInterval(fertilizingIntervalText)) {
//                            Text("Введите число больше 0", color = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = sdf.format(Date(lastFertilizedMillis)),
//                    onValueChange = { /* only read */ },
//                    label = { Text("Последнее удобрение") },
//                    modifier = Modifier.fillMaxWidth(),
//                    readOnly = true,
//                    trailingIcon = {
//                        IconButton(onClick = { showFertilizingDatePicker = true }) {
//                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
//                        }
//                    }
//                )
//            }
//
//            item {
//                Button(
//                    onClick = { showReferenceDialog = true },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Заполнить из справочника")
//                }
//            }
//
//            item {
//                Button(
//                    onClick = {
//                        if (isFormValid) {
//                            val wateringDays = wateringIntervalText.toInt()
//                            val fertilizingDays = fertilizingIntervalText.toInt()
//
//                            scope.launch {
//                                if (plantId == null) {
//                                    val newPlant = Plant(
//                                        name = name,
//                                        description = if (description.isBlank()) null else description,
//                                        wateringIntervalDays = wateringDays,
//                                        lastWatered = lastWateredMillis,
//                                        fertilizingIntervalDays = fertilizingDays,
//                                        lastFertilized = lastFertilizedMillis,
//                                        imagePath = imagePath // ← сохраняем!
//                                    )
//                                    plantDao.insert(newPlant)
//                                } else {
//                                    val existingPlant = plantDao.getPlantById(plantId)
//                                    if (existingPlant != null) {
//                                        val updatedPlant = existingPlant.copy(
//                                            name = name,
//                                            description = if (description.isBlank()) null else description,
//                                            wateringIntervalDays = wateringDays,
//                                            lastWatered = lastWateredMillis,
//                                            fertilizingIntervalDays = fertilizingDays,
//                                            lastFertilized = lastFertilizedMillis,
//                                            imagePath = imagePath // ← обновляем!
//                                        )
//                                        plantDao.update(updatedPlant)
//                                    }
//                                }
//                                onSaved()
//                            }
//                        }
//                    },
//                    enabled = isFormValid,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(if (plantId == null) "Создать растение" else "Сохранить изменения")
//                }
//            }
//
//            item {
//                OutlinedButton(
//                    onClick = { showPhotoSourceDialog = true },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Добавить фото растения")
//                }
//            }
//
//
//
////                item {
////                    OutlinedButton(
////                        onClick = { imagePickerLauncher.launch("image/*") },
////                        modifier = Modifier.fillMaxWidth()
////                    ) {
////                        Text("Добавить фото растения")
////                    }
////                }
//
//                item {
//                    imagePath?.let { uriString ->
//                        val painter = rememberAsyncImagePainter(
//                            model = Uri.parse(uriString),
//                            contentScale = ContentScale.Crop
//                        )
//                        Image(
//                            painter = painter,
//                            contentDescription = "Фото растения",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp)
//                                .clip(RoundedCornerShape(8.dp)),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//                }
//        }
//    }
//
//    if (showPhotoSourceDialog) {
//        AlertDialog(
//            onDismissRequest = { showPhotoSourceDialog = false },
//            title = { Text("Выберите источник") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        showPhotoSourceDialog = false
//                        imagePickerLauncher.launch("image/*")
//                    }
//                ) { Text("Галерея") }
//            },
//            dismissButton = {
//                TextButton(
//                    onClick = {
//                        showPhotoSourceDialog = false
//                        openCamera() // ← вызываем функцию
//                    }
//                ) { Text("Камера") }
//            }
//        )
//    }
//
//    // DatePicker для полива
//    if (showWateringDatePicker) {
//        val datePickerState = rememberDatePickerState(
//            initialSelectedDateMillis = lastWateredMillis
//        )
//
//        DatePickerDialog(
//            onDismissRequest = { showWateringDatePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        datePickerState.selectedDateMillis?.let {
//                            lastWateredMillis = it
//                        }
//                        showWateringDatePicker = false
//                    }
//                ) {
//                    Text("OK")
//                }
//            }
//        ) {
//            DatePicker(state = datePickerState)
//        }
//    }
//
//    // DatePicker для удобрения
//    if (showFertilizingDatePicker) {
//        val datePickerState = rememberDatePickerState(
//            initialSelectedDateMillis = lastFertilizedMillis
//        )
//
//        DatePickerDialog(
//            onDismissRequest = { showFertilizingDatePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        datePickerState.selectedDateMillis?.let {
//                            lastFertilizedMillis = it
//                        }
//                        showFertilizingDatePicker = false
//                    }
//                ) {
//                    Text("OK")
//                }
//            }
//        ) {
//            DatePicker(state = datePickerState)
//        }
//    }
//
//    // Справочник
//    if (showReferenceDialog) {
//        ReferenceSearchDialog(
//            onDismiss = { showReferenceDialog = false },
//            onPlantSelected = { refPlant ->
//                name = refPlant.name
//                description = refPlant.description
//                wateringIntervalText = refPlant.wateringIntervalDays.toString()
//                fertilizingIntervalText = refPlant.fertilizerIntervalDays.toString()
//                showReferenceDialog = false
//            }
//        )
//    }
//
//
//
//
//
//
//
//    // 👇 Диалог: Галерея или Камера?
//
//
//}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddEditPlantScreen(
//    plantId: Long? = null,
//    onSaved: () -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val db = AppDatabase.getInstance(context)
//    val plantDao = db.plantDao()
//
//    var name by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var wateringIntervalText by remember { mutableStateOf("3") }
//    var fertilizingIntervalText by remember { mutableStateOf("30") }
//
//    var lastWateredMillis by remember { mutableStateOf(System.currentTimeMillis()) }
//    var lastFertilizedMillis by remember { mutableStateOf(System.currentTimeMillis()) }
//    var showReferenceDialog by remember { mutableStateOf(false) }
//    var showWateringDatePicker by remember { mutableStateOf(false) }
//    var showFertilizingDatePicker by remember { mutableStateOf(false) }
//
//    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
//
//    var imagePath by remember { mutableStateOf<String?>(null) }
//
//    // === ДОБАВЛЕНО: для работы с камерой ===
//    var photoFile by remember { mutableStateOf<File?>(null) }
//    var showPhotoSourceDialog by remember { mutableStateOf(false) }
//
//    // Валидация: число должно быть положительным
//    val isValidInterval: (String) -> Boolean = { text ->
//        text.toIntOrNull()?.let { it > 0 } == true
//    }
//
//    // Полная валидация формы
//    val isFormValid = remember(name, wateringIntervalText, fertilizingIntervalText) {
//        name.isNotBlank() &&
//                isValidInterval(wateringIntervalText) &&
//                isValidInterval(fertilizingIntervalText)
//    }
//
//    // Загрузка данных при редактировании
//    LaunchedEffect(plantId) {
//        if (plantId != null && plantId != 0L) {
//            val plant = plantDao.getPlantById(plantId)
//            if (plant != null) {
//                name = plant.name
//                description = plant.description ?: ""
//                wateringIntervalText = plant.wateringIntervalDays.toString()
//                fertilizingIntervalText = plant.fertilizingIntervalDays.toString()
//                lastWateredMillis = plant.lastWatered
//                lastFertilizedMillis = plant.lastFertilized
//                imagePath = plant.imagePath
//            }
//        }
//    }
//
//    // === ДОБАВЛЕНО: Launchers для галереи и камеры ===
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        uri?.let { imagePath = it.toString() }
//    }
//
//    val cameraLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.TakePicture()
//    ) { success ->
//        if (success && photoFile != null) {
//            imagePath = Uri.fromFile(photoFile!!).toString()
//        }
//    }
//
//    // Функция создания файла
//    fun createImageFile(context: Context): File {
//        return File.createTempFile(
//            "plant_${System.currentTimeMillis()}_",
//            ".jpg",
//            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        ).apply { photoFile = this }
//    }
//
//    val requestPermissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            photoFile = createImageFile(context)
//            cameraLauncher.launch(photoFile?.let { Uri.fromFile(it) })
//        } else {
//            Toast.makeText(context, "Нужен доступ к камере для съёмки", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//
//    // Функция открытия камеры с запросом разрешения
//    val openCameraWithPermission = {
//        when {
//            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> {
//                // На Android < 6 разрешения не нужны
//                photoFile = createImageFile(context)
//                cameraLauncher.launch(photoFile?.let { Uri.fromFile(it) })
//            }
//            ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.CAMERA
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                photoFile = createImageFile(context)
//                cameraLauncher.launch(photoFile?.let { Uri.fromFile(it) })
//            }
//            else -> {
//                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
//            }
//        }
//    }
//
//    val scope = rememberCoroutineScope()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (plantId == null) "Добавить растение" else "Редактировать растение") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
//            item {
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Название растения") },
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = name.isBlank(),
//                    supportingText = {
//                        if (name.isBlank()) {
//                            Text("Обязательное поле", color = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = description,
//                    onValueChange = { description = it },
//                    label = { Text(stringResource(R.string.plant_description_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = false,
//                    maxLines = 3
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = wateringIntervalText,
//                    onValueChange = { wateringIntervalText = it },
//                    label = { Text(stringResource(R.string.watering_interval_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    isError = wateringIntervalText.isNotBlank() && !isValidInterval(wateringIntervalText),
//                    supportingText = {
//                        if (wateringIntervalText.isNotBlank() && !isValidInterval(wateringIntervalText)) {
//                            Text("Введите число больше 0", color = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = sdf.format(Date(lastWateredMillis)),
//                    onValueChange = { /* only read */ },
//                    label = { Text(stringResource(R.string.last_watered_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    readOnly = true,
//                    trailingIcon = {
//                        IconButton(onClick = { showWateringDatePicker = true }) {
//                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = fertilizingIntervalText,
//                    onValueChange = { fertilizingIntervalText = it },
//                    label = { Text("Интервал удобрения (дней)") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                    isError = fertilizingIntervalText.isNotBlank() && !isValidInterval(fertilizingIntervalText),
//                    supportingText = {
//                        if (fertilizingIntervalText.isNotBlank() && !isValidInterval(fertilizingIntervalText)) {
//                            Text("Введите число больше 0", color = MaterialTheme.colorScheme.error)
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = sdf.format(Date(lastFertilizedMillis)),
//                    onValueChange = { /* only read */ },
//                    label = { Text("Последнее удобрение") },
//                    modifier = Modifier.fillMaxWidth(),
//                    readOnly = true,
//                    trailingIcon = {
//                        IconButton(onClick = { showFertilizingDatePicker = true }) {
//                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
//                        }
//                    }
//                )
//            }
//
//            item {
//                Button(
//                    onClick = { showReferenceDialog = true },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Заполнить из справочника")
//                }
//            }
//
//            // === ДОБАВЛЕНО: Кнопка добавления фото ===
//            item {
//                OutlinedButton(
//                    onClick = { showPhotoSourceDialog = true },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Добавить фото растения")
//                }
//            }
//
//            // === ДОБАВЛЕНО: Превью изображения ===
//            item {
//                imagePath?.let { uriString ->
//                    val painter = rememberAsyncImagePainter(
//                        model = Uri.parse(uriString),
//                        contentScale = ContentScale.Crop
//                    )
//                    Image(
//                        painter = painter,
//                        contentDescription = "Фото растения",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp)
//                            .clip(RoundedCornerShape(8.dp))
//                    )
//                }
//            }
//
//            item {
//                Button(
//                    onClick = {
//                        if (isFormValid) {
//                            val wateringDays = wateringIntervalText.toInt()
//                            val fertilizingDays = fertilizingIntervalText.toInt()
//
//                            scope.launch {
//                                if (plantId == null) {
//                                    val newPlant = Plant(
//                                        name = name,
//                                        description = if (description.isBlank()) null else description,
//                                        wateringIntervalDays = wateringDays,
//                                        lastWatered = lastWateredMillis,
//                                        fertilizingIntervalDays = fertilizingDays,
//                                        lastFertilized = lastFertilizedMillis,
//                                        imagePath = imagePath // ← сохраняем!
//                                    )
//                                    plantDao.insert(newPlant)
//                                } else {
//                                    val existingPlant = plantDao.getPlantById(plantId)
//                                    if (existingPlant != null) {
//                                        val updatedPlant = existingPlant.copy(
//                                            name = name,
//                                            description = if (description.isBlank()) null else description,
//                                            wateringIntervalDays = wateringDays,
//                                            lastWatered = lastWateredMillis,
//                                            fertilizingIntervalDays = fertilizingDays,
//                                            lastFertilized = lastFertilizedMillis,
//                                            imagePath = imagePath // ← обновляем!
//                                        )
//                                        plantDao.update(updatedPlant)
//                                    }
//                                }
//                                onSaved()
//                            }
//                        }
//                    },
//                    enabled = isFormValid,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(if (plantId == null) "Создать растение" else "Сохранить изменения")
//                }
//            }
//        }
//    }
//
//    // DatePicker для полива
//    if (showWateringDatePicker) {
//        val datePickerState = rememberDatePickerState(
//            initialSelectedDateMillis = lastWateredMillis
//        )
//
//        DatePickerDialog(
//            onDismissRequest = { showWateringDatePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        datePickerState.selectedDateMillis?.let {
//                            lastWateredMillis = it
//                        }
//                        showWateringDatePicker = false
//                    }
//                ) {
//                    Text("OK")
//                }
//            }
//        ) {
//            DatePicker(state = datePickerState)
//        }
//    }
//
//    // DatePicker для удобрения
//    if (showFertilizingDatePicker) {
//        val datePickerState = rememberDatePickerState(
//            initialSelectedDateMillis = lastFertilizedMillis
//        )
//
//        DatePickerDialog(
//            onDismissRequest = { showFertilizingDatePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        datePickerState.selectedDateMillis?.let {
//                            lastFertilizedMillis = it
//                        }
//                        showFertilizingDatePicker = false
//                    }
//                ) {
//                    Text("OK")
//                }
//            }
//        ) {
//            DatePicker(state = datePickerState)
//        }
//    }
//
//    // === ДОБАВЛЕНО: Диалог выбора источника фото ===
//    if (showPhotoSourceDialog) {
//        AlertDialog(
//            onDismissRequest = { showPhotoSourceDialog = false },
//            title = { Text("Выберите источник") },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        showPhotoSourceDialog = false
//                        imagePickerLauncher.launch("image/*")
//                    }
//                ) { Text("Галерея") }
//            },
//            dismissButton = {
//                TextButton(
//                    onClick = {
//                        showPhotoSourceDialog = false
//                        openCameraWithPermission()
//                    }
//                ) { Text("Камера") }
//            }
//        )
//    }
//
//    // Справочник
//    if (showReferenceDialog) {
//        ReferenceSearchDialog(
//            onDismiss = { showReferenceDialog = false },
//            onPlantSelected = { refPlant ->
//                name = refPlant.name
//                description = refPlant.description
//                wateringIntervalText = refPlant.wateringIntervalDays.toString()
//                fertilizingIntervalText = refPlant.fertilizerIntervalDays.toString()
//                showReferenceDialog = false
//            }
//        )
//    }
//}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AddEditPlantScreen(
//    plantId: Long? = null,
//    onSaved: () -> Unit,
//    onBack: () -> Unit
//) {
//    val context = LocalContext.current
//    val db = AppDatabase.getInstance(context)
//    val plantDao = db.plantDao()
//
//    var name by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var wateringInterval by remember { mutableStateOf(3) }
//    var fertilizingInterval by remember { mutableStateOf(33) }
//    var lastWateredMillis by remember { mutableStateOf(System.currentTimeMillis()) }
//    var lastFertilizedMillis by remember { mutableStateOf(System.currentTimeMillis()) }
//    var showReferenceDialog by remember { mutableStateOf(false) }
//    var showWateringDatePicker by remember { mutableStateOf(false) }
//    var showFertilizingDatePicker by remember { mutableStateOf(false) }
//
//    var selectedFields by remember { mutableStateOf(mutableSetOf<String>()) }
//
//    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
//
//    // Загрузка данных при редактировании
//    LaunchedEffect(plantId) {
//        if (plantId != null && plantId != 0L) {
//            val plant = plantDao.getPlantById(plantId)
//            if (plant != null) {
//                name = plant.name
//                description = plant.description ?: "" // ← даже если null — будет пустая строка
//                wateringInterval = plant.wateringIntervalDays
//                fertilizingInterval = plant.fertilizingIntervalDays
//                lastWateredMillis = plant.lastWatered
//                lastFertilizedMillis = plant.lastFertilized
//            }
//        }
//    }
//
//    val scope = rememberCoroutineScope()
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text(if (plantId == null) "Добавить растение" else "Редактировать растение") },
//                navigationIcon = {
//                    IconButton(onClick = onBack) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
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
//            item {
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Название растения") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = description,
//                    onValueChange = { description = it },
//                    label = { Text(stringResource(R.string.plant_description_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = false,
//                    maxLines = 3
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = wateringInterval.toString(),
//                    onValueChange = {
//                        it.toIntOrNull()?.let { days -> if (days > 0) wateringInterval = days }
//                    },
//                    label = { Text(stringResource(R.string.watering_interval_label)) },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = sdf.format(Date(lastWateredMillis)),
//                    onValueChange = { /* только чтение */ },
//                    label = { Text(stringResource(R.string.last_watered_label)) },
//                    modifier = Modifier.fillMaxWidth(),
//                    readOnly = true,
//                    trailingIcon = {
//                        IconButton(onClick = { showWateringDatePicker = true }) {
//                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
//                        }
//                    }
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = fertilizingInterval.toString(),
//                    onValueChange = {
//                        it.toIntOrNull()?.let { days -> if (days > 0) fertilizingInterval = days }
//                    },
//                    label = { Text("Интервал удобрения (дней)") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//            }
//
//            item {
//                OutlinedTextField(
//                    value = sdf.format(Date(lastFertilizedMillis)),
//                    onValueChange = { /* только чтение */ },
//                    label = { Text("Последнее удобрение") },
//                    modifier = Modifier.fillMaxWidth(),
//                    readOnly = true,
//                    trailingIcon = {
//                        IconButton(onClick = { showFertilizingDatePicker = true }) {
//                            Icon(Icons.Default.CalendarToday, contentDescription = stringResource(R.string.cd_select_date))
//                        }
//                    }
//                )
//            }
//
//            item {
//                Button(
//                    onClick = { showReferenceDialog = true },
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text("Заполнить из справочника")
//                }
//            }
//
//            item {
//                Button(
//                    onClick = {
//                        if (name.isNotBlank()) {
//                            scope.launch {
//                                if (plantId == null) {
//                                    // Создание нового растения
//                                    val newPlant = Plant(
//                                        name = name,
//                                        description = if (description.isBlank()) null else description,
//                                        wateringIntervalDays = wateringInterval,
//                                        lastWatered = lastWateredMillis,
//                                        fertilizingIntervalDays = fertilizingInterval,
//                                        lastFertilized = lastFertilizedMillis
//                                    )
//                                    plantDao.insert(newPlant)
//                                } else {
//                                    // Обновление существующего
//                                    val existingPlant = plantDao.getPlantById(plantId)
//                                    if (existingPlant != null) {
//                                        val updatedPlant = existingPlant.copy(
//                                            name = name,
//                                            description = if (description.isBlank()) null else description,
//                                            wateringIntervalDays = wateringInterval,
//                                            lastWatered = lastWateredMillis,
//                                            fertilizingIntervalDays = fertilizingInterval,
//                                            lastFertilized = lastFertilizedMillis
//                                        )
//                                        plantDao.update(updatedPlant)
//                                    }
//                                }
//                                onSaved()
//                            }
//                        }
//                    },
//                    enabled = name.isNotBlank(),
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(if (plantId == null) "Создать растение" else "Сохранить изменения")
//                }
//            }
//        }
//    }
//
//    // DatePicker для полива
//    if (showWateringDatePicker) {
//        val datePickerState = rememberDatePickerState(
//            initialSelectedDateMillis = lastWateredMillis
//        )
//
//        DatePickerDialog(
//            onDismissRequest = { showWateringDatePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        datePickerState.selectedDateMillis?.let {
//                            lastWateredMillis = it
//                        }
//                        showWateringDatePicker = false
//                    }
//                ) {
//                    Text("OK")
//                }
//            }
//        ) {
//            DatePicker(state = datePickerState)
//        }
//    }
//
//// Для удобрения — аналогично
//    if (showFertilizingDatePicker) {
//        val datePickerState = rememberDatePickerState(
//            initialSelectedDateMillis = lastFertilizedMillis
//        )
//
//        DatePickerDialog(
//            onDismissRequest = { showFertilizingDatePicker = false },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        datePickerState.selectedDateMillis?.let {
//                            lastFertilizedMillis = it
//                        }
//                        showFertilizingDatePicker = false
//                    }
//                ) {
//                    Text("OK")
//                }
//            }
//        ) {
//            DatePicker(state = datePickerState)
//        }
//    }
//
//    // Справочник
//    if (showReferenceDialog) {
//        ReferenceSearchDialog(
//            onDismiss = { showReferenceDialog = false },
//            onPlantSelected = { refPlant ->
//                // Автоматически заполняем ВСЕ поля
//                name = refPlant.name
//                description = refPlant.description
//                wateringInterval = refPlant.wateringIntervalDays
//                fertilizingInterval = refPlant.fertilizerIntervalDays
//                // Даты ОСТАЮТСЯ как есть (не трогаем lastWatered/lastFertilized)
//                showReferenceDialog = false
//            }
//        )
//    }
//}