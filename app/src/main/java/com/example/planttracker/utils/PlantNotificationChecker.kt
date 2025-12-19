// utils/PlantNotificationChecker.kt
package com.example.planttracker.utils

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.example.planttracker.database.AppDatabase
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

suspend fun checkAndShowPendingWateringNotifications(context: Context) {
    if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
        return
    }

    val db = AppDatabase.getInstance(context)
    val plants = withContext(Dispatchers.IO) {
        db.plantDao().getAllPlants().first() // твой Flow -> List
    }

    val now = System.currentTimeMillis()
    val millsInDay = TimeUnit.DAYS.toMillis(1)

    for (plant in plants) {
        val daysSinceWatering = (now - plant.lastWatered) / millsInDay
        if (daysSinceWatering >= plant.wateringIntervalDays) {
            NotificationHelper.showWateringReminder(
                context,
                plant.name,
                plant.id
            )
        }
    }
}