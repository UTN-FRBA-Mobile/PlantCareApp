package com.example.plant_care_app.testing

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TestingInfrastructureTest {

    @Test
    fun `test data factory creates overridable plant data`() {
        val plant = TestDataFactory.plantOverview(
            id = "plant-42",
            name = "Pothos",
            soilMoisture = 31
        )

        assertEquals("plant-42", plant.id)
        assertEquals("Pothos", plant.name)
        assertEquals(31, plant.soilMoisture)
    }

    @Test
    fun `mockk works with suspend functions`() = runTest {
        val service = mockk<TestPlantService>()
        val plant = TestDataFactory.plantOverview(name = "Helecho")

        coEvery { service.getPlant("plant-1") } returns plant

        val result = service.getPlant("plant-1")

        assertEquals("Helecho", result.name)
        coVerify(exactly = 1) { service.getPlant("plant-1") }
    }

    private interface TestPlantService {
        suspend fun getPlant(id: String): com.example.plant_care_app.ui.models.PlantOverviewDto
    }
}
