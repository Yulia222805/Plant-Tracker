package com.example.planttracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.Plant
import com.example.planttracker.repository.PlantRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PlantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlantRepository

    val allPlants: StateFlow<List<Plant>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = PlantRepository(db)

        allPlants = repository.allPlants
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun addPlant(plant: Plant) {
        viewModelScope.launch {
            repository.insert(plant)
        }
    }

    fun updatePlant(plant: Plant) {
        viewModelScope.launch {
            repository.update(plant)
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            repository.delete(plant)
        }
    }
}