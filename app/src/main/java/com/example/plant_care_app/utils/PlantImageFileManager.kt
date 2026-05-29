package com.example.plant_care_app.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object PlantImageFileManager {

    fun createImageFile(
        context: Context,
        plantId: String? = null
    ): File {
        val dir = File(context.filesDir, "plant_images")
            .also { it.mkdirs() }

        val prefix = plantId ?: "temp"

        return File(
            dir,
            "plant_${prefix}_${System.currentTimeMillis()}.jpg"
        )
    }

    fun getUriForFile(
        context: Context,
        file: File
    ): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    fun copyUriToPlantImageFile(
        context: Context,
        uri: Uri,
        plantId: String? = null
    ): File {
        val file = createImageFile(context, plantId)

        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return file
    }
}