// database/ReferencePlant.kt
package com.example.planttracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reference_plants")
data class ReferencePlant(
    @PrimaryKey val id: Int,
    val name: String,
    val wateringIntervalDays: Int,
    val lightInfo: String,
    val fertilizerIntervalDays: Int,
    val description: String
)