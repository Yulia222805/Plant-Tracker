// utils/AppSettingsManager.kt
package com.example.planttracker.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationManagerCompat

//class AppSettingsManager(private val context: Context) {
//    private val prefs = context.getSharedPreferences("plant_tracker_prefs", Context.MODE_PRIVATE)
//
//    var isReferenceDbInitialized: Boolean
//        get() = prefs.getBoolean("is_reference_db_initialized", false)
//        set(value) = prefs.edit().putBoolean("is_reference_db_initialized", value).apply()
//
//    // ← НОВОЕ свойство:
//    var isWateringWorkerScheduled: Boolean
//        get() = prefs.getBoolean("is_watering_worker_scheduled", false)
//        set(value) = prefs.edit().putBoolean("is_watering_worker_scheduled", value).apply()
//
//    var areNotificationsEnabled: Boolean
//        get() = prefs.getBoolean("notifications_enabled", true)
//        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()
//
//    var notificationsEnabled: Boolean
//        get() = prefs.getBoolean("notifications_enabled", true)
//        set(value) = prefs.edit().putBoolean("notifications_enabled", value).apply()
//
//    var notificationsUserEnabled: Boolean
//        get() = prefs.getBoolean("notifications_user_enabled", true)
//        set(value) = prefs.edit().putBoolean("notifications_user_enabled", value).apply()
//
//    fun areNotificationsActuallyEnabled(): Boolean {
//        val systemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
//        val userEnabled = notificationsUserEnabled
//        return systemEnabled && userEnabled
//    }
//}

class AppSettingsManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("plant_tracker_prefs", Context.MODE_PRIVATE)


    private val CURRENT_DB_VERSION = 3 // ← должно совпадать с @Database(version = 3)

    init {
        // Автоматически сбрасываем флаг, если версия базы изменилась
        val savedDbVersion = prefs.getInt("db_version", 0)
        if (savedDbVersion < CURRENT_DB_VERSION) {
            prefs.edit()
                .putBoolean("is_reference_db_initialized", false)
                .putInt("db_version", CURRENT_DB_VERSION)
                .apply()
        }
    }

    var isReferenceDbInitialized: Boolean
        get() = prefs.getBoolean("is_reference_db_initialized", false)
        set(value) = prefs.edit().putBoolean("is_reference_db_initialized", value).apply()

    var isWateringWorkerScheduled: Boolean
        get() = prefs.getBoolean("is_watering_worker_scheduled", false)
        set(value) = prefs.edit().putBoolean("is_watering_worker_scheduled", value).apply()

    var notificationsUserEnabled: Boolean
        get() = prefs.getBoolean("notifications_user_enabled", true)
        set(value) = prefs.edit().putBoolean("notifications_user_enabled", value).apply()

    // Логическое состояние: будут ли уведомления показываться?
    fun areNotificationsLogicallyEnabled(): Boolean {
        val systemEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        return systemEnabled && notificationsUserEnabled
    }
}