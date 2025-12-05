package com.example.planttracker

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


// при открытии приложения
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { // ранее сохраненное состояние
        super.onCreate(savedInstanceState)
        setContent {
            PlantTrackerApp()
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantTrackerApp() {
    val navController = rememberNavController() // переход между экранами
    val drawerState = rememberDrawerState(DrawerValue.Closed) //состояние drawer
    val scope = rememberCoroutineScope()

    // Состояние для отслеживания текущего экрана (нужно для заголовка)
    var currentScreen by remember { //запоминает состояние, а то при каждом обновлении UI сбрасывался бы до первоначального состояния
        mutableStateOf<PlantScreen>(PlantScreen.MyPlants)
    } //для перерисовки названия

    ModalNavigationDrawer( //боковое меню поверх экрана
        drawerState = drawerState, // открыт закрыт
        drawerContent = {
            ModalDrawerSheet { // обертка - будут стили и отступы
                DrawerItem( //передаем иконку, текст и действие
                    icon = Icons.Default.Home,
                    label = "Мои растения"
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
            }
        }
    ) { // что видно, когда меню закрыто
        Scaffold( // базовая структура макета
            topBar = { TopAppBar(title = { Text(currentScreen.title) }) }
        ) { innerPadding ->
            NavHost( // контейнер для экранов навигации
                navController = navController,
                startDestination = PlantScreen.MyPlants.route, // с какого экрана начинать
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(PlantScreen.MyPlants.route) {
                    MyPlantsScreen()
                }
                composable(PlantScreen.WateringCalendar.route) {
                    WateringCalendarScreen()
                }
                composable(PlantScreen.Guide.route) {
                    GuideScreen()
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
}

// Экраны
@Composable
fun MyPlantsScreen() {
    androidx.compose.material3.Surface(Modifier.fillMaxSize()) {
        androidx.compose.material3.Text(
            text = "Экран: Мои растения",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun WateringCalendarScreen() {
    androidx.compose.material3.Surface(Modifier.fillMaxSize()) {
        androidx.compose.material3.Text(
            text = "Экран: Календарь полива",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun GuideScreen() {
    androidx.compose.material3.Surface(Modifier.fillMaxSize()) {
        androidx.compose.material3.Text(
            text = "Экран: Справочник",
            modifier = Modifier.padding(16.dp)
        )
    }
}


@Preview(name = "Мои растения", showBackground = true)
@Composable
fun PreviewMyPlantsScreen() {
    MyPlantsScreen()
}

@Preview(name = "Календарь полива", showBackground = true)
@Composable
fun PreviewWateringCalendarScreen() {
        WateringCalendarScreen()
}

@Preview(name = "Справочник", showBackground = true)
@Composable
fun PreviewGuideScreen() {
        GuideScreen()
}

//package com.example.planttracker
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.planttracker.ui.theme.PlantTrackerTheme
//
//import androidx.appcompat.app.AppCompatActivity           // основной класс активити
//import androidx.navigation.findNavController        // функция для поиска NavController
//import androidx.navigation.ui.AppBarConfiguration   // настройка поведения ActionBar
//import androidx.navigation.ui.setupActionBarWithNavController  // связывает Toolbar с навигацией
//import androidx.navigation.ui.setupWithNavController // связывает NavigationView с навигацией
//import com.example.planttracker.databinding.ActivityMainBinding  // связь с activity_main.xml
//
//
//
//
//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            PlantTrackerTheme {
//                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    PlantTrackerTheme {
//        Greeting("Android")
//    }
//}