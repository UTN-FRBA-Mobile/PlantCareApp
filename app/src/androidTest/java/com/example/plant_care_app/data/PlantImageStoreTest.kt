package com.example.plant_care_app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlantImageStoreTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clearTestPlantImages()
    }

    @After
    fun tearDown() {
        clearTestPlantImages()
    }

    @Test
    fun saveImagePathStoresPathForPlant() {
        PlantImageStore.saveImagePath(
            context = context,
            plantId = "plant-image-1",
            imagePath = "/tmp/plant-image-1.jpg"
        )

        val result = PlantImageStore.getImagePath(context, "plant-image-1")

        assertEquals("/tmp/plant-image-1.jpg", result)
    }

    @Test
    fun saveImagePathKeepsDifferentPlantsSeparated() {
        PlantImageStore.saveImagePath(context, "plant-image-1", "/tmp/plant-image-1.jpg")
        PlantImageStore.saveImagePath(context, "plant-image-2", "/tmp/plant-image-2.jpg")

        assertEquals("/tmp/plant-image-1.jpg", PlantImageStore.getImagePath(context, "plant-image-1"))
        assertEquals("/tmp/plant-image-2.jpg", PlantImageStore.getImagePath(context, "plant-image-2"))
    }

    @Test
    fun removeImagePathRemovesOnlySelectedPlantPath() {
        PlantImageStore.saveImagePath(context, "plant-image-1", "/tmp/plant-image-1.jpg")
        PlantImageStore.saveImagePath(context, "plant-image-2", "/tmp/plant-image-2.jpg")

        PlantImageStore.removeImagePath(context, "plant-image-1")

        assertNull(PlantImageStore.getImagePath(context, "plant-image-1"))
        assertEquals("/tmp/plant-image-2.jpg", PlantImageStore.getImagePath(context, "plant-image-2"))
    }

    @Test
    fun getImagePathReturnsNullWhenNoPathWasSaved() {
        val result = PlantImageStore.getImagePath(context, "plant-image-1")

        assertNull(result)
    }

    private fun clearTestPlantImages() {
        listOf("plant-image-1", "plant-image-2").forEach { plantId ->
            PlantImageStore.removeImagePath(context, plantId)
        }
    }
}
