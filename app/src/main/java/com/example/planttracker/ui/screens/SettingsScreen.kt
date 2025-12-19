// ui/screens/SettingsScreen.kt
package com.example.planttracker.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.example.planttracker.utils.AppSettingsManager
import android.app.NotificationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
//import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.planttracker.R
import com.example.planttracker.utils.checkAndShowPendingWateringNotifications
import com.example.planttracker.workers.CheckWateringWorker
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope


fun openNotificationSettings(
    context: Context,
    launcher: ActivityResultLauncher<Intent>
) {
    val intent = Intent().apply {
        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    launcher.launch(intent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var notificationsEnabled by remember {
        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
    }

    // Запоминаем, были ли уведомления включены ДО открытия настроек
    var wasEnabled by remember { mutableStateOf(notificationsEnabled) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val nowEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

        // 🔔 Если уведомления были выключены, а теперь включены — проверяем напоминания
        if (!wasEnabled && nowEnabled) {
            scope.launch {
                checkAndShowPendingWateringNotifications(context)
            }
        }

        // Обновляем состояния
        wasEnabled = nowEnabled
        notificationsEnabled = nowEnabled
    }

    LaunchedEffect(Unit) {
        val current = NotificationManagerCompat.from(context).areNotificationsEnabled()
        notificationsEnabled = current
        wasEnabled = current
    }

    Scaffold(
        topBar = {
            // TopAppBar(title = { Text("Настройки") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Уведомления",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            openNotificationSettings(context, launcher)
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(stringResource(R.string.settings_notifications_title))
                        Text(
                            text = if (notificationsEnabled) "Уведомления включены" else "Уведомления отключены",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = null
                    )
                }
            }
        }
    }
}


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SettingsScreen() {
//    val context = LocalContext.current
//
//    // ✅ observable состояние
//    var notificationsEnabled by remember {
//        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
//    }
//
//    val launcher = rememberLauncherForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) {
//        // ✅ обновляем после возврата
//        notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
//    }
//
//    // ✅ перечитываем при каждом входе на экран (на случай, если пользователь
//    //    открыл/закрыл настройки, а потом снова зашёл в SettingsScreen)
//    LaunchedEffect(Unit) {
//        notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
//    }
//
//    Scaffold(
//        topBar = {
//            // TopAppBar(title = { Text("Настройки") })
//        }
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            item {
//                Text(
//                    text = "Уведомления",
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//            }
//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp)
//                        .clickable {
//                            openNotificationSettings(context, launcher)
//                        },
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(stringResource(R.string.settings_notifications_title))
//                        Text(
//                            text = if (notificationsEnabled) "Уведомления включены" else "Уведомления отключены",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                    Switch(
//                        checked = notificationsEnabled,
//                        onCheckedChange = null
//                    )
//                }
//            }
//        }
//    }
//}
//
//private fun openNotificationSettings(
//    context: Context,
//    launcher: ActivityResultLauncher<Intent>
//) {
//    val intent = Intent().apply {
//        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
//        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
//    }
//    launcher.launch(intent)
//}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SettingsScreen(
//    settings: AppSettingsManager,
//    requestPermission: () -> Unit,
//    openNotificationSettings: () -> Unit
//) {
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//
//    var uiState by remember {
//        mutableStateOf(
//            NotificationUiState(
//                systemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
//                userEnabled = settings.notificationsUserEnabled
//            )
//        )
//    }
//
//
//
//    LaunchedEffect(lifecycleOwner) {
//        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//            val newSystemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
//            uiState = uiState.copy(systemEnabled = newSystemEnabled)
//        }
//    }
//
//    val switchChecked = uiState.systemEnabled && uiState.userEnabled
//    val notificationManager = NotificationManagerCompat.from(context)
//    var notificationsEnabled by remember {
//        mutableStateOf(notificationManager.areNotificationsEnabled())
//    }
//
//
//    // ✅ Используем State, который можно обновить извне
//    var systemEnabled by remember {
//        mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled())
//    }
//
//    // 🔁 Обновляем при каждом входе в приложение (защита от ручных изменений в настройках)
//    LaunchedEffect(lifecycleOwner) {
//        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//            systemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
//        }
//    }
//
//    // Если у тебя есть флаг пользователя:
//    val userEnabled = settings.notificationsUserEnabled
////    val switchChecked = systemEnabled && userEnabled
//
//    Scaffold(
//        topBar = {
//            // TopAppBar(title = { Text("Настройки") })
//        }
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text("Уведомления")
//                        Text(
//                            text = if (notificationsEnabled) "Включены" else "Отключены",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                    Switch(
//                        checked = switchChecked,
//                        onCheckedChange = { enabled ->
//                            if (enabled) {
//                                if (!systemEnabled) {
//                                    requestPermission()
//                                    // После запроса — UI обновится через LaunchedEffect выше
//                                }
//                                settings.notificationsUserEnabled = true
//                            } else {
//                                openNotificationSettings()
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}

private data class NotificationUiState(
    val systemEnabled: Boolean,
    val userEnabled: Boolean
)


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SettingsScreen(settings: AppSettingsManager) {
//    var notificationsEnabled by remember {
//        mutableStateOf(settings.areNotificationsEnabled)
//    }
//
//    // Синхронизируем с настройками приложения
//    LaunchedEffect(Unit) {
//        notificationsEnabled = settings.areNotificationsEnabled
//    }
//
//    Scaffold(
//        topBar = {
//            // TopAppBar(title = { Text("Настройки") })
//        }
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            item {
//                Text(
//                    text = "Уведомления",
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//            }
//            item {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .clickable {
//                            val newValue = !notificationsEnabled
//                            notificationsEnabled = newValue
//                            settings.areNotificationsEnabled = newValue
//                        }
//                        .padding(vertical = 8.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text(stringResource(R.string.settings_notifications_title))
//                        Text(
//                            text = stringResource(R.string.settings_notifications_summary),
//                            style = MaterialTheme.typography.bodySmall,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                    Switch(
//                        checked = notificationsEnabled,
//                        onCheckedChange = { newValue ->
//                            notificationsEnabled = newValue
//                            settings.areNotificationsEnabled = newValue
//                        }
//                    )
//                }
//            }
//        }
//    }
//}

//@Composable
//fun SettingsScreen(settings: AppSettingsManager) {
//    val context = LocalContext.current
//    val notificationManager = NotificationManagerCompat.from(context)
////    val notificationsEnabled = notificationManager.areNotificationsEnabled()
////    var notificationsEnabled by remember {
////        mutableStateOf(settings.areNotificationsEnabled)
////    }
//    var notificationsEnabled by remember {
//        mutableStateOf(settings.areNotificationsActuallyEnabled())
//    }
//
//    LaunchedEffect(Unit) {
//        notificationsEnabled = settings.areNotificationsActuallyEnabled()
//    }
//
//    Scaffold(
//        topBar = {
////            TopAppBar(title = { Text("Настройки") })
//        }
//    ) { padding ->
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding),
//            contentPadding = PaddingValues(16.dp)
//        ) {
//            item {
//                Text(
//                    text = "Уведомления",
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//            }
//            item {
//                ListItem(
//                    headlineContent = { Text(stringResource(R.string.settings_notifications_title)) },
//                    supportingContent = { Text(stringResource(R.string.settings_notifications_summary)) },
//                    trailingContent = {
////                        Switch(
////                            checked = notificationsEnabled,
////                            onCheckedChange = { enabled ->
////                                if (enabled) {
////                                    settings.notificationsUserEnabled = true
////                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
////                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
////                                    }
////                                    context.startActivity(intent)
////                                } else {
////                                    settings.notificationsUserEnabled = false
////                                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////                                    nm.cancelAll()
////                                    notificationsEnabled = false
////                                }
////                            }
////                        )
////                        Switch(
////                            checked = notificationsEnabled,
////                            onCheckedChange = { enabled ->
////                                notificationsEnabled = enabled
////                                settings.notificationsUserEnabled = enabled
//////                                if (enabled) {
//////                                    // Пользователь хочет включить → открываем системные настройки
//////                                    settings.notificationsUserEnabled = true
//////                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
//////                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
//////                                    }
//////                                    context.startActivity(intent)
//////                                }
////
////                                if (!enabled) {
////                                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////                                    nm.cancelAll()
////                                } else {
////                                    // Если включил — открываем системные настройки, чтобы включить разрешение
////                                    WorkManager.getInstance(context)
////                                        .enqueue(OneTimeWorkRequestBuilder<CheckWateringWorker>().build())
////                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
////                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
////                                    }
////                                    context.startActivity(intent)
////                                }
////                                notificationsEnabled = settings.areNotificationsActuallyEnabled()
////                            }
////                        )
////                        Switch(
////                            checked = notificationsEnabled,
////                            onCheckedChange = { enabled ->
////                                notificationsEnabled = enabled          // ← обновляем состояние → Switch двигается!
////                                settings.notificationsEnabled = enabled // ← сохраняем в настройки
////                                if (!enabled) {
////                                    // Отменяем все уведомления
////                                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
////                                    nm.cancelAll()
////                                }
////                            }
//////                            checked = notificationsEnabled,
//////                            onCheckedChange = { enabled ->
//////                                if (!enabled) {
//////                                    // Просто отключаем — уведомления перестанут приходить
//////                                    // Но включить программно нельзя → открываем настройки
//////                                } else {
//////                                    // Перенаправляем в системные настройки
//////                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
//////                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
//////                                    }
//////                                    context.startActivity(intent)
//////                                }
//////                            }
////                        )
//                    }
//                )
//            }
//        }
//    }
//}