package com.example.planttracker

//import com.example.planttracker.ui.screens.MyPlantsScreen
//import com.example.planttracker.ui.screens.AddPlantScreenContent

import com.example.planttracker.ui.screens.PlantDetailScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
//import androidx.constraintlayout.widget.Constraints
//import androidx.compose.ui.unit.Constraints
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.Plant
import kotlinx.coroutines.flow.collectLatest
import com.example.planttracker.utils.AppSettingsManager
import com.example.planttracker.ui.screens.GuideScreen
import androidx.lifecycle.lifecycleScope
import com.example.planttracker.data.ReferencePlantData
import com.example.planttracker.ui.WateringCalendarScreen
import com.example.planttracker.ui.screens.AddEditPlantScreen
import com.example.planttracker.ui.screens.MyPlantsScreen
import com.example.planttracker.ui.screens.AddPlantScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.planttracker.utils.NotificationHelper
import com.example.planttracker.workers.CheckWateringWorker
import java.util.concurrent.TimeUnit

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.planttracker.ui.screens.PlantDetailScreen
import com.example.planttracker.ui.screens.SettingsScreen
import android.provider.Settings
import android.content.Intent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// При открытии приложения

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            proceedWithApp()


    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Nothing to do — UI сам обновится, так как читает системное состояние
    }

    private fun openNotificationSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }


    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            proceedWithApp()
        } else {
            // Просто запрашиваем разрешение — без предварительного диалога
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_NOTIFICATION_PERMISSION -> {
                // НЕ меняем notificationsUserEnabled!
                // Пользователь мог просто отложить решение.
                // Мы просто продолжаем запуск.
                proceedWithApp()
            }
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            REQUEST_NOTIFICATION_PERMISSION -> {
//                val granted = grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED
//                val settings = AppSettingsManager(this)
//                settings.notificationsUserEnabled = granted // ← сохраняем выбор пользователя
//                proceedWithApp()
//            }
//        }
//    }

    private fun proceedWithApp() {
        Log.d("MainActivity", "ЗАПУСК MainActivity")

        val settings = AppSettingsManager(this)
        val db = AppDatabase.getInstance(this)

        NotificationHelper.createNotificationChannel(this)

        lifecycleScope.launch {
            // Инициализация справочника
//            if (!settings.isReferenceDbInitialized) {
//                Log.d("MainActivity", "Инициализация справочника...")
//                withContext(Dispatchers.IO) {
//                    db.referencePlantDao().insertAll(ReferencePlantData.plants)
//                }
//                settings.isReferenceDbInitialized = true
//            }
            if (!settings.isReferenceDbInitialized) {
                Log.d("MainActivity", "Инициализация справочника...")
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        db.referencePlantDao().insertAll(ReferencePlantData.plants)
                    }
                    settings.isReferenceDbInitialized = true
                    Log.d("MainActivity", "Справочник инициализирован!")
                }
            } else {
                Log.d("MainActivity", "Справочник уже инициализирован")
            }

            // Планирование worker'а
            if (!settings.isWateringWorkerScheduled) {
                withContext(Dispatchers.IO) {
                    scheduleWateringCheck()
                }
                settings.isWateringWorkerScheduled = true
            }

            // Только теперь показываем UI
            withContext(Dispatchers.Main) {
                setContent {
                    PlantTrackerApp(settings = settings)
                }
            }
        }

//        if (!settings.isReferenceDbInitialized) {
//            lifecycleScope.launch {
//                db.referencePlantDao().insertAll(ReferencePlantData.plants)
//                settings.isReferenceDbInitialized = true
//            }
//        }
//
//        if (!settings.isWateringWorkerScheduled) {
//            scheduleWateringCheck()
//            settings.isWateringWorkerScheduled = true
//        }
//
//        setContent {
//            PlantTrackerApp(settings = settings)
//
//        }
    }

    private fun scheduleWateringCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val wateringRequest = PeriodicWorkRequestBuilder<CheckWateringWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "WateringReminderWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                wateringRequest
            )
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 101
    }


}

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) { // ранее сохраненное состояние
//        super.onCreate(savedInstanceState)
//        val settings = AppSettingsManager(this)
//        val db = AppDatabase.getInstance(this)
//
//        NotificationHelper.createNotificationChannel(this)
//
//        if (!settings.isReferenceDbInitialized) {
//            lifecycleScope.launch {
//                db.referencePlantDao().insertAll(ReferencePlantData.plants)
//                settings.isReferenceDbInitialized = true
//            }
//        }
//
//        if (!settings.isWateringWorkerScheduled) {
//            scheduleWateringCheck()
//            settings.isWateringWorkerScheduled = true
//        }
//
//        setContent {
//            PlantTrackerApp(settings = settings)
//        }
//
//    }
//    private fun scheduleWateringCheck() {
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
//            .build()
//
//        val wateringRequest = PeriodicWorkRequestBuilder<CheckWateringWorker>(
//            24, TimeUnit.HOURS,
//            1, TimeUnit.HOURS
//        )
//            .setConstraints(constraints)
//            .build()
//
//        WorkManager.getInstance(this)
//            .enqueueUniquePeriodicWork(
//                "WateringReminderWorker",
//                ExistingPeriodicWorkPolicy.KEEP,
//                wateringRequest
//            )
//    }
////    override fun onDestroy() {
////        super.onDestroy()
////        lifecycleScope.cancel()
////    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantTrackerApp(settings: AppSettingsManager,
                    ) {
    val navController = rememberNavController() // переход между экранами
    val drawerState = rememberDrawerState(DrawerValue.Closed) //состояние drawer
    val scope = rememberCoroutineScope()

    // Состояние для отслеживания текущего экрана (нужно для заголовка)
    var currentScreen by remember { //запоминает состояние, а то при каждом обновлении UI сбрасывался бы до первоначального состояния
        mutableStateOf<PlantScreen>(PlantScreen.MyPlants)
    } //для перерисовки названия

    val context = LocalContext.current
    val db = AppDatabase.getInstance(context)
    val plantDao = db.plantDao()

    ModalNavigationDrawer( //боковое меню поверх экрана
        drawerState = drawerState, // открыт закрыт
        drawerContent = {
            ModalDrawerSheet { // обертка - будут стили и отступы
                DrawerItem( //передаем иконку, текст и действие
                    icon = Icons.Default.Home,
                    label = stringResource(R.string.drawer_my_plants)
                ) {
                    currentScreen = PlantScreen.MyPlants
                    // перейди на экран типа с таким маршрутом
                    navController.navigate(PlantScreen.MyPlants.route) {
                        // Очищаем стек навигации
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                }
                DrawerItem(
                    icon = Icons.Default.Event,
                    label = "Календарь полива"
                ) {
                    currentScreen = PlantScreen.WateringCalendar
                    navController.navigate(PlantScreen.WateringCalendar.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() } //корутина плавно закрывает drawer
                }
                DrawerItem(
                    icon = Icons.Default.Info,
                    label = "Справочник"
                ) {
                    currentScreen = PlantScreen.Guide
                    navController.navigate(PlantScreen.Guide.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                }
                DrawerItem(
                    icon = Icons.Default.Settings,
                    label = "Настройки"
                ) {
                    currentScreen = PlantScreen.Settings
                    navController.navigate(PlantScreen.Settings.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                    scope.launch { drawerState.close() }
                }
            }
        }
    ) { // что видно, когда меню закрыто
        Scaffold( // базовая структура макета
//            topBar = { TopAppBar(title = { Text(currentScreen.title) }) }
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    }
                )
            }
        ) { innerPadding ->
//            NavHost( // контейнер для экранов навигации
//                navController = navController,
//                startDestination = PlantScreen.MyPlants.route, // с какого экрана начинать
//                modifier = Modifier.padding(innerPadding)
//            ) {
////                composable(PlantScreen.MyPlants.route) {
////                    MyPlantsScreen()
////                }
////                composable(PlantScreen.MyPlants.route) {
////                    MyPlantsScreen(
////                        navigateToAddPlant = {
////                            navController.navigate(PlantScreen.AddPlant.route)
////                        }
////                    )
////                }
////                onEditPlantClick = { plantId ->
////                    navController.navigate("edit_plant/$plantId")
////                }
////
////
////
//////                composable(PlantScreen.AddPlant.route) {
//////                    AddPlantScreen(
//////                        onPlantSaved = {
//////                            navController.popBackStack()
//////                        },
//////                        onBack = {
//////                            navController.popBackStack()
//////                        }
//////                    )
//////                }
////                composable(PlantScreen.WateringCalendar.route) {
////                    WateringCalendarScreen()
////                }
////                composable(PlantScreen.Guide.route) {
////                    GuideScreen()
////                }
////                composable("add_plant") {
////                    AddEditPlantScreen(
////                        onSaved = { navController.popBackStack() },
////                        onBack = { navController.popBackStack() }
////                    )
////                }
////
////                composable("edit_plant/{plantId}") { backStackEntry ->
////                    val plantId = backStackEntry.arguments?.getLong("plantId")
////                    AddEditPlantScreen(
////                        plantId = plantId,
////                        onSaved = { navController.popBackStack() },
////                        onBack = { navController.popBackStack() }
////                    )
////                }
//                composable("plant_detail/{plantId}") { backStackEntry ->
//                    val plantId = backStackEntry.arguments?.getLong("plantId") ?: return@composable
//                    PlantDetailScreen(
//                        plantId = plantId,
//                        onBack = { navController.popBackStack() },
//                        onEdit = { navController.navigate("edit_plant/$plantId") }
//                    )
//                }
////                composable(PlantScreen.MyPlants.route) {
////                    MyPlantsScreen(
////                        navigateToAddPlant = {
////                            navController.navigate(PlantScreen.AddPlant.route)
////                        },
////                        onEditPlantClick = { plantId ->
////                            navController.navigate("edit_plant/$plantId")
////                        }
////                    )
////                }
//                composable(PlantScreen.WateringCalendar.route) {
//                    WateringCalendarScreen()
//                }
//                composable(PlantScreen.Guide.route) {
//                    GuideScreen()
//                }
//                composable("add_plant") {
//                    AddEditPlantScreen(
//                        onSaved = { navController.popBackStack() },
//                        onBack = { navController.popBackStack() }
//                    )
//                }
//                composable(
//                    "edit_plant/{plantId}",
//                    arguments = listOf(navArgument("plantId") { type = NavType.LongType })
//                ) { backStackEntry ->
//                    val plantId = backStackEntry.arguments?.getLong("plantId") ?: 0L
//                    AddEditPlantScreen(
//                        plantId = if (plantId == 0L) null else plantId,
//                        onSaved = { navController.popBackStack() },
//                        onBack = { navController.popBackStack() }
//                    )
//                }
////                composable("plant_detail/{plantId}") { backStackEntry ->
////                    val plantId = backStackEntry.arguments?.getLong("plantId") ?: 0L
////                    PlantDetailScreen(
////                        plantId = plantId,
////                        onBack = { navController.popBackStack() },
////                        onEdit = { navController.navigate("edit_plant/$plantId") }
////                    )
////                }
//            }
            NavHost(
                navController = navController,
                startDestination = PlantScreen.MyPlants.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(PlantScreen.MyPlants.route) {
                    MyPlantsScreen(
                        navigateToAddPlant = {
                            navController.navigate("add_plant")
                        },
                        navigateToPlantDetail = { plantId ->
                            navController.navigate("plant_detail/$plantId")
                        },
//                        onEditPlantClick = { plantId ->
//                            navController.navigate("edit_plant/$plantId")
//                        }
                    )
                }

                composable(PlantScreen.WateringCalendar.route) {
                    WateringCalendarScreen()
                }

                composable(PlantScreen.Guide.route) {
                    GuideScreen()
                }

                composable("add_plant") {
                    AddEditPlantScreen(
                        onSaved = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "edit_plant/{plantId}",
                    arguments = listOf(navArgument("plantId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val plantId = backStackEntry.arguments?.getLong("plantId") ?: 0L
                    AddEditPlantScreen(
                        plantId = if (plantId == 0L) null else plantId,
                        onSaved = { navController.popBackStack() },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    "plant_detail/{plantId}",
                    arguments = listOf(navArgument("plantId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val plantId = backStackEntry.arguments?.getLong("plantId")
                        ?: return@composable // безопасная проверка
                    PlantDetailScreen(
                        plantId = plantId,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate("edit_plant/$plantId") }
                    )
                }
//                composable(PlantScreen.Settings.route) { SettingsScreen(settings = settings) }
                composable(route = "settings") {
                    SettingsScreen()
                }
            }
        }
    }
}

// Элемент меню в Drawer
@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.NavigationDrawerItem(
        icon = { androidx.compose.material3.Icon(icon, contentDescription = null) },
        label = { Text(label) },
        selected = false,
        onClick = onClick
    )
}

// Перечисление экранов
sealed class PlantScreen(val route: String, val title: String) {
    object MyPlants : PlantScreen("my_plants", "Мои растения")
    object WateringCalendar : PlantScreen("watering_calendar", "Календарь полива")
    object Guide : PlantScreen("guide", "Справочник")
    object AddPlant : PlantScreen("add_plant", "Добавить растение")
    object Settings : PlantScreen("settings", "Настройки")
}

// Экраны
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
//    // Загружаем растения в реальном времени
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

//@Composable
//fun PlantItem(
//    plant: Plant,
//    onEdit: () -> Unit,
//    onDelete: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//            .clickable { onEdit() },
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Column {
//            Text(
//                text = plant.name,
//                style = MaterialTheme.typography.titleMedium
//            )
//            if (!plant.description.isNullOrEmpty()) {
//                Text(
//                    text = plant.description!!,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//        }
//        Row {
//            IconButton(onClick = onEdit) {
//                Icon(Icons.Default.Edit, contentDescription = "Редактировать")
//            }
//            IconButton(onClick = onDelete) {
//                Icon(Icons.Default.Delete, contentDescription = "Удалить")
//            }
//        }
//    }
//}
//
//@Composable
//fun AddEditPlantDialog(
//    plant: Plant?,
//    onConfirm: (String) -> Unit,
//    onDismiss: () -> Unit
//) {
//    var name by remember { mutableStateOf(plant?.name ?: "") }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        title = { Text(if (plant != null) "Редактировать растение" else "Добавить растение") },
//        text = {
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                TextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    label = { Text("Название растения") },
//                    modifier = Modifier.fillMaxWidth()
//                )
//                // Можно добавить поле для описания позже
//            }
//        },
//        confirmButton = {
//            Button(
//                enabled = name.isNotBlank(),
//                onClick = {
//                    onConfirm(name)
//                }
//            ) {
//                Text("Сохранить")
//            }
//        },
//        dismissButton = {
//            Button(onClick = onDismiss) {
//                Text("Отмена")
//            }
//        }
//    )
//}

//@Composable
//fun WateringCalendarScreen() {
//    androidx.compose.material3.Surface(Modifier.fillMaxSize()) {
//        androidx.compose.material3.Text(
//            text = "Экран: Календарь полива",
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//}

//@Composable
//fun GuideScreen() {
//    androidx.compose.material3.Surface(Modifier.fillMaxSize()) {
//        androidx.compose.material3.Text(
//            text = "Экран: Справочник",
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//}

//package com.example.planttracker
//
////import com.example.planttracker.ui.screens.MyPlantsScreen
////import com.example.planttracker.ui.screens.AddPlantScreenContent
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Event
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.Info
//import androidx.compose.material3.DrawerValue
//import androidx.compose.material3.ModalDrawerSheet
//import androidx.compose.material3.ModalNavigationDrawer
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.material3.rememberDrawerState
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.unit.dp
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import kotlinx.coroutines.launch
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.ui.tooling.preview.Preview
//
//
//// при открытии приложения
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) { // ранее сохраненное состояние
//        super.onCreate(savedInstanceState)
//        setContent {
//            PlantTrackerApp()
//        }
//    }
//}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun PlantTrackerApp() {
//    val navController = rememberNavController() // переход между экранами
//    val drawerState = rememberDrawerState(DrawerValue.Closed) //состояние drawer
//    val scope = rememberCoroutineScope()
//
//    // Состояние для отслеживания текущего экрана (нужно для заголовка)
//    var currentScreen by remember { //запоминает состояние, а то при каждом обновлении UI сбрасывался бы до первоначального состояния
//        mutableStateOf<PlantScreen>(PlantScreen.MyPlants)
//    } //для перерисовки названия
//
//    ModalNavigationDrawer( //боковое меню поверх экрана
//        drawerState = drawerState, // открыт закрыт
//        drawerContent = {
//            ModalDrawerSheet { // обертка - будут стили и отступы
//                DrawerItem( //передаем иконку, текст и действие
//                    icon = Icons.Default.Home,
//                    label = "Мои растения"
//                ) {
//                    currentScreen = PlantScreen.MyPlants
//                    // перейди на экран типа с таким маршрутом
//                    navController.navigate(PlantScreen.MyPlants.route) {
//                        // Очищаем стек навигации
//                        popUpTo(navController.graph.startDestinationId) {
//                            saveState = true
//                        }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                    scope.launch { drawerState.close() }
//                }
//                DrawerItem(
//                    icon = Icons.Default.Event,
//                    label = "Календарь полива"
//                ) {
//                    currentScreen = PlantScreen.WateringCalendar
//                    navController.navigate(PlantScreen.WateringCalendar.route) {
//                        popUpTo(navController.graph.startDestinationId) {
//                            saveState = true
//                        }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                    scope.launch { drawerState.close() } //корутина плавно закрывает drawer
//                }
//                DrawerItem(
//                    icon = Icons.Default.Info,
//                    label = "Справочник"
//                ) {
//                    currentScreen = PlantScreen.Guide
//                    navController.navigate(PlantScreen.Guide.route) {
//                        popUpTo(navController.graph.startDestinationId) {
//                            saveState = true
//                        }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                    scope.launch { drawerState.close() }
//                }
//            }
//        }
//    ) { // что видно, когда меню закрыто
//        Scaffold( // базовая структура макета
//            topBar = { TopAppBar(title = { Text(currentScreen.title) }) }
//        ) { innerPadding ->
////            NavHost(
////                navController = navController,
////                startDestination = PlantScreen.MyPlants.route,
////                modifier = Modifier.padding(innerPadding)
////            ) {
////                composable(PlantScreen.MyPlants.route) {
////                    MyPlantsScreen(navigateToAddPlant = {
////                        navController.navigate("add_plant")
////                    })
////                }
////                composable("add_plant") {
////                    AddPlantScreenContent(onPlantSaved = {
////                        navController.navigateUp()
////                    })
////                }
////                composable(PlantScreen.WateringCalendar.route) {
////                    WateringCalendarScreen()
////                }
////                composable(PlantScreen.Guide.route) {
////                    GuideScreen()
////                }
////            }
//                    NavHost( // контейнер для экранов навигации
//                navController = navController,
//                startDestination = PlantScreen.MyPlants.route, // с какого экрана начинать
//                modifier = Modifier.padding(innerPadding)
//            ) {
//                composable(PlantScreen.MyPlants.route) {
//                    MyPlantsScreen()
//                }
//                composable(PlantScreen.WateringCalendar.route) {
//                    WateringCalendarScreen()
//                }
//                composable(PlantScreen.Guide.route) {
//                    GuideScreen()
//                }
//            }
//        }
//    }
//}
//
//// Элемент меню в Drawer
//@Composable
//fun DrawerItem(
//    icon: ImageVector,
//    label: String,
//    onClick: () -> Unit
//) {
//    androidx.compose.material3.NavigationDrawerItem(
//        icon = { androidx.compose.material3.Icon(icon, contentDescription = null) },
//        label = { Text(label) },
//        selected = false,
//        onClick = onClick
//    )
//}
//
//// Перечисление экранов
//sealed class PlantScreen(val route: String, val title: String) {
//    object MyPlants : PlantScreen("my_plants", "Мои растения")
//    object WateringCalendar : PlantScreen("watering_calendar", "Календарь полива")
//    object Guide : PlantScreen("guide", "Справочник")
//}
//
//// Экраны
//@Composable
//fun MyPlantsScreen() {
//    androidx.compose.material3.Surface(Modifier.fillMaxSize()) {
//        androidx.compose.material3.Text(
//            text = "Экран: Мои растения",
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//}
//
//@Composable
//fun WateringCalendarScreen() {
//    androidx.compose.material3.Surface(Modifier.fillMaxSize()) {
//        androidx.compose.material3.Text(
//            text = "Экран: Календарь полива",
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//}
//
//@Composable
//fun GuideScreen() {
//    androidx.compose.material3.Surface(Modifier.fillMaxSize()) {
//        androidx.compose.material3.Text(
//            text = "Экран: Справочник",
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//}
//
//
////@Preview(name = "Мои растения", showBackground = true)
////@Composable
////fun PreviewMyPlantsScreen() {
//////    MyPlantsScreen()
//////    MyPlantsScreen(navigateToAddPlant = {})
////}
//
////@Preview(name = "Календарь полива", showBackground = true)
////@Composable
////fun PreviewWateringCalendarScreen() {
////        WateringCalendarScreen()
////}
////
////@Preview(name = "Справочник", showBackground = true)
////@Composable
////fun PreviewGuideScreen() {
////        GuideScreen()
////}
//
////package com.example.planttracker
////
////import android.os.Bundle
////import androidx.activity.ComponentActivity
////import androidx.activity.compose.setContent
////import androidx.activity.enableEdgeToEdge
////import androidx.compose.foundation.layout.fillMaxSize
////import androidx.compose.foundation.layout.padding
////import androidx.compose.material3.Scaffold
////import androidx.compose.material3.Text
////import androidx.compose.runtime.Composable
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.tooling.preview.Preview
////import com.example.planttracker.ui.theme.PlantTrackerTheme
////
////import androidx.appcompat.app.AppCompatActivity           // основной класс активити
////import androidx.navigation.findNavController        // функция для поиска NavController
////import androidx.navigation.ui.AppBarConfiguration   // настройка поведения ActionBar
////import androidx.navigation.ui.setupActionBarWithNavController  // связывает Toolbar с навигацией
////import androidx.navigation.ui.setupWithNavController // связывает NavigationView с навигацией
////import com.example.planttracker.databinding.ActivityMainBinding  // связь с activity_main.xml
////
////
////
////
////class MainActivity : ComponentActivity() {
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        enableEdgeToEdge()
////        setContent {
////            PlantTrackerTheme {
////                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
////                    Greeting(
////                        name = "Android",
////                        modifier = Modifier.padding(innerPadding)
////                    )
////                }
////            }
////        }
////    }
////}
////
////@Composable
////fun Greeting(name: String, modifier: Modifier = Modifier) {
////    Text(
////        text = "Hello $name!",
////        modifier = modifier
////    )
////}
////
////@Preview(showBackground = true)
////@Composable
////fun GreetingPreview() {
////    PlantTrackerTheme {
////        Greeting("Android")
////    }
////}