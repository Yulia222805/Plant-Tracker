// repository/PlantRepository.kt
package com.example.planttracker.repository

import androidx.lifecycle.LiveData
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.Plant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow

class PlantRepository(private val db: AppDatabase) {
    private val plantDao = db.plantDao()

    val allPlants: Flow<List<Plant>> = db.plantDao().getAllPlants()

    suspend fun insert(plant: Plant) = plantDao.insert(plant)

    suspend fun update(plant: Plant) = plantDao.update(plant)

    suspend fun delete(plant: Plant) = plantDao.delete(plant)

    suspend fun getPlantById(id: Long): Plant? = plantDao.getPlantById(id)
}