package com.example.planttracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.planttracker.R
import com.example.planttracker.receivers.WateringNotificationReceiver

object NotificationHelper {
    const val CHANNEL_ID = "plant_watering_reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Напоминания о поливе",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления, когда пора полить растение"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showWateringReminder(context: Context, plantName: String, plantId: Long) {
        // Уникальный requestCode на основе plantId
        val baseRequestCode = plantId.hashCode()

        // === Кнопка "Полито" ===
        val wateredIntent = Intent(context, WateringNotificationReceiver::class.java).apply {
            action = WateringNotificationReceiver.ACTION_WATERED
            putExtra(WateringNotificationReceiver.EXTRA_PLANT_ID, plantId)
        }
        val wateredPendingIntent = PendingIntent.getBroadcast(
            context,
            baseRequestCode,
            wateredIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // === Кнопка "Прочитано" ===
        val dismissIntent = Intent(context, WateringNotificationReceiver::class.java).apply {
            action = WateringNotificationReceiver.ACTION_DISMISSED
            putExtra(WateringNotificationReceiver.EXTRA_PLANT_ID, plantId)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            baseRequestCode + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // === Сборка уведомления ===
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name) // ← замени на свою иконку или используй android.R.drawable.ic_dialog_info
            .setContentTitle("Пора полить $plantName!")
            .setContentText("Не забудьте полить ваше растение сегодня.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // автоматически закрывать при нажатии на уведомление или кнопки
            .addAction(
                android.R.drawable.ic_menu_save, // ✅ иконка "галочка" (можно заменить)
                "Полито",
                wateredPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel, // ❌ иконка "крестик"
                "Прочитано",
                dismissPendingIntent
            )

        // Показываем уведомление с уникальным ID (на основе plantId)
        with(NotificationManagerCompat.from(context)) {
            notify(plantId.toInt(), builder.build())
        }
    }
}