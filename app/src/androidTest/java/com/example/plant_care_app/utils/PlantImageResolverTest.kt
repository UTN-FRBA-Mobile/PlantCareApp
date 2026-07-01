package com.example.plant_care_app.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.plant_care_app.data.PlantImageStore
import com.example.plant_care_app.data.RetrofitClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PlantImageResolverTest {

    private lateinit var context: Context
    private val createdFiles = mutableListOf<File>()

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clearPlantImageStore()
    }

    @After
    fun tearDown() {
        clearPlantImageStore()
        createdFiles.forEach { it.delete() }
    }

    @Test
    fun resolveReturnsExistingLocalImagePathBeforeRemoteImageUrl() {
        val localImage = createImageFile("local-plant.jpg")

        val result = PlantImageResolver.resolve(
            context = context,
            plantId = "plant-local-path",
            imageUrl = "https://example.com/remote.jpg",
            localImagePath = localImage.absolutePath
        )

        assertEquals(localImage, result)
    }

    @Test
    fun resolveReturnsStoredLocalImageWhenLocalImagePathIsNotProvided() {
        val storedImage = createImageFile("stored-plant.jpg")
        PlantImageStore.saveImagePath(
            context = context,
            plantId = "plant-stored-path",
            imagePath = storedImage.absolutePath
        )

        val result = PlantImageResolver.resolve(
            context = context,
            plantId = "plant-stored-path",
            imageUrl = "https://example.com/remote.jpg"
        )

        assertEquals(storedImage, result)
    }

    @Test
    fun resolveReturnsAbsoluteRemoteImageUrlWhenNoLocalImageExists() {
        val imageUrl = "https://example.com/plant.jpg"

        val result = PlantImageResolver.resolve(
            context = context,
            plantId = "plant-absolute-url",
            imageUrl = imageUrl
        )

        assertEquals(imageUrl, result)
    }

    @Test
    fun resolveBuildsAbsoluteUrlForRemoteImagePath() {
        val result = PlantImageResolver.resolve(
            context = context,
            plantId = "plant-relative-url",
            imageUrl = "/uploads/plant.jpg"
        )

        assertEquals(
            RetrofitClient.BASE_URL.trimEnd('/') + "/uploads/plant.jpg",
            result
        )
    }

    @Test
    fun resolveReturnsNullWhenRemoteImageUrlIsBlankOrInvalid() {
        val blankResult = PlantImageResolver.resolve(
            context = context,
            plantId = "plant-blank-url",
            imageUrl = "   "
        )
        val invalidResult = PlantImageResolver.resolve(
            context = context,
            plantId = "plant-invalid-url",
            imageUrl = "uploads/plant.jpg"
        )

        assertNull(blankResult)
        assertNull(invalidResult)
    }

    @Test
    fun resolveIgnoresMissingLocalImagePathAndUsesRemoteImageUrl() {
        val missingLocalImage = File(context.cacheDir, "missing-plant.jpg")
        val imageUrl = "https://example.com/fallback.jpg"

        val result = PlantImageResolver.resolve(
            context = context,
            plantId = "plant-missing-local",
            imageUrl = imageUrl,
            localImagePath = missingLocalImage.absolutePath
        )

        assertTrue(!missingLocalImage.exists())
        assertEquals(imageUrl, result)
    }

    private fun createImageFile(name: String): File {
        val file = File(context.cacheDir, name)
        file.writeText("test image")
        createdFiles += file
        return file
    }

    private fun clearPlantImageStore() {
        listOf(
            "plant-local-path",
            "plant-stored-path",
            "plant-absolute-url",
            "plant-relative-url",
            "plant-blank-url",
            "plant-invalid-url",
            "plant-missing-local"
        ).forEach { plantId ->
            PlantImageStore.removeImagePath(context, plantId)
        }
    }
}
