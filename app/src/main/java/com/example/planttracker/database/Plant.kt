package com.example.planttracker.database

import androidx.room.*

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String? = null,

    @ColumnInfo(name = "watering_interval_days") val wateringIntervalDays: Int = 3,
    @ColumnInfo(name = "last_watered") val lastWatered: Long, // ← БЕЗ = System.currentTimeMillis()

    @ColumnInfo(name = "fertilizing_interval_days") val fertilizingIntervalDays: Int = 30,
    @ColumnInfo(name = "last_fertilized") val lastFertilized: Long, // ← БЕЗ дефолта

    @ColumnInfo(name = "image_path") val imagePath: String? = null
)