// utils/PreferencesManager.kt
package com.example.planttracker.utils

import android.content.Context
import android.content.SharedPreferences

object PreferencesManager {
    private const val PREF_NAME = "plant_tracker_prefs"
    private const val KEY_WORKER_SCHEDULED = "is_watering_worker_scheduled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isWateringWorkerScheduled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_WORKER_SCHEDULED, false)
    }

    fun setWateringWorkerScheduled(context: Context, scheduled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_WORKER_SCHEDULED, scheduled).apply()
    }
}