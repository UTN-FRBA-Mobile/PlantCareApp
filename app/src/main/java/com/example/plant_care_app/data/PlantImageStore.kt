package com.example.plant_care_app.data

import android.content.Context

object PlantImageStore {

    private const val PREFS_NAME = "plant_images"

    fun saveImagePath(context: Context, plantId: String, imagePath: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(plantId, imagePath)
            .apply()
    }

    fun getImagePath(context: Context, plantId: String): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(plantId, null)
    }

    fun removeImagePath(context: Context, plantId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(plantId)
            .apply()
    }
}