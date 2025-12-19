// database/ReferencePlantDao.kt
package com.example.planttracker.database

import androidx.room.*

@Dao
interface ReferencePlantDao {
    @Query("SELECT * FROM reference_plants")
    suspend fun getAllPlants(): List<ReferencePlant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plants: List<ReferencePlant>)

    @Query("SELECT COUNT(*) FROM reference_plants")
    suspend fun getCount(): Int

    @Query("SELECT * FROM reference_plants WHERE name LIKE '%' || :name || '%'")
    suspend fun findByName(name: String): ReferencePlant?

    @Query("SELECT * FROM reference_plants WHERE name LIKE :query || '%'")
    suspend fun searchByName(query: String): List<ReferencePlant>
}