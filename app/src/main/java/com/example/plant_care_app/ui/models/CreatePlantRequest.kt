package com.example.plant_care_app.ui.models

data class CreatePlantRequest(
    val name: String,
    val species: String,
    val location: String,
    val imageUrl: String? = null,
    val sensorId: String? = null
)
