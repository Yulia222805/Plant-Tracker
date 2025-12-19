package com.example.planttracker

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.planttracker.database.AppDatabase
import com.example.planttracker.database.Plant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.flow.first

@RunWith(AndroidJUnit4::class)
class PlantDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: com.example.planttracker.database.PlantDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // только для тестов!
            .build()
        dao = db.plantDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun testInsertAndGetPlant() = runTest {
        // Дано
        val plant = Plant(name = "Орхидея", description = "Цветок")

        // Когда
        dao.insert(plant)
        val allPlants = dao.getAllPlants().first()

        // Тогда
        assertEquals(1, allPlants.size)
        assertEquals("Орхидея", allPlants[0].name)
        assertNotNull(allPlants[0].id)
    }

    @Test
    fun testUpdatePlant() = runTest {
        // Дано
        val plant = Plant(name = "Кактус")
        dao.insert(plant)
        val inserted = dao.getAllPlants().first().first()

        // Когда
        val updatedPlant = inserted.copy(name = "Кактус колючий")
        dao.update(updatedPlant)
        val result = dao.getAllPlants().first().first()

        // Тогда
        assertEquals("Кактус колючий", result.name)
    }

    @Test
    fun testDeletePlant() = runTest {
        // Дано
        val plant = Plant(name = "Папоротник")
        dao.insert(plant)
        val inserted = dao.getAllPlants().first().first()

        // Когда
        dao.delete(inserted)
        val result = dao.getAllPlants().first()

        // Тогда
        assertTrue(result.isEmpty())
    }
}