package com.example.planttracker.workers

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.utils.AppSettingsManager
import com.example.planttracker.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CheckWateringWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // 🔸 Проверяем настройки
            val settings = AppSettingsManager(applicationContext)
//            if (!settings.notificationsEnabled) {
//                Log.d("CheckWateringWorker", "Уведомления отключены в настройках → пропускаем")
//                return Result.success()
//            }

//            if (!settings.areNotificationsLogicallyEnabled()) {
//                return Result.success() // не показываем
//            }

            if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
                return Result.success()
            }

            // 🔸 Получаем базу
            val db = AppDatabase.getInstance(applicationContext)

            // 🔸 Получаем СПИСОК растений из Flow (один раз!)
            val plants = db.plantDao().getAllPlants().first() // ← first() из Flow

            val now = System.currentTimeMillis()
            val millsInDay = TimeUnit.DAYS.toMillis(1)

            Log.d("CheckWateringWorker", "Всего растений: ${plants.size}")

            for (plant in plants) {
                val daysSinceWatering = (now - plant.lastWatered) / millsInDay
                if (daysSinceWatering >= plant.wateringIntervalDays) {
                    Log.d("CheckWateringWorker", "→ Напоминание: ${plant.name}")
                    NotificationHelper.showWateringReminder(
                        applicationContext,
                        plant.name,
                        plant.id
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e("CheckWateringWorker", "Ошибка", e)
            Result.failure()
        }
    }
}

//class CheckWateringWorker(
//    context: Context,
//    params: WorkerParameters
//) : CoroutineWorker(context, params) {
//
//    override suspend fun doWork(): Result {
//        Log.d("TEST", "Worker !")
//        return try {
//            val db = AppDatabase.getInstance(applicationContext)
//            val now = System.currentTimeMillis()
//            val plants = db.plantDao().getPlantsNeedingWater(now)
//
//            Log.d("CheckWateringWorker", "Найдено растений для полива: ${plants.size}")
//            for (plant in plants) {
//                Log.d("CheckWateringWorker", "→ ${plant.name}")
//                NotificationHelper.showWateringReminder(applicationContext, plant.name, plant.id)
//            }
//
//            Result.success()
//        } catch (e: Exception) {
//            Log.e("CheckWateringWorker", "Ошибка", e)
//            Result.failure()
//        }
//    }
//}