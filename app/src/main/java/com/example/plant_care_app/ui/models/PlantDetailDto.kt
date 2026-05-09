package com.example.plant_care_app.ui.models

data class PlantDetailDto(
    val id: String,
    val name: String,
    val species: String,
    val location: String,
    val imageUrl: String?,
    val sensorId: String?,
    val lastWateringAt: String?
)
