// receivers/WateringNotificationReceiver.kt
package com.example.planttracker.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.workers.CheckWateringWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WateringNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_WATERED = "ACTION_WATERED"
        const val ACTION_DISMISSED = "ACTION_DISMISSED"
        const val EXTRA_PLANT_ID = "EXTRA_PLANT_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val plantId = intent.getLongExtra(EXTRA_PLANT_ID, -1L)
        val notificationId = plantId.toInt()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        when (action) {
            ACTION_DISMISSED -> {
                Log.d("Notification", "Уведомление закрыто пользователем")
            }

            ACTION_WATERED -> {
                if (plantId != -1L) {
                    Log.d("Notification", "Растение $plantId полили!")
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getInstance(context)
                        val plant = db.plantDao().getPlantById(plantId)
                        if (plant != null) {
                            val updatedPlant = plant.copy(lastWatered = System.currentTimeMillis())
                            db.plantDao().update(updatedPlant)
                            Log.d("Notification", "Дата полива обновлена")


                            WorkManager.getInstance(context)
                                .enqueue(OneTimeWorkRequestBuilder<CheckWateringWorker>().build())
                        }
                    }
                }
            }
        }
    }
}