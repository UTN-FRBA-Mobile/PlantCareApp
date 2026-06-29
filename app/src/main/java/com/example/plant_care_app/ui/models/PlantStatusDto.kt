package com.example.plant_care_app.ui.models

data class PlantStatusDto(
    val statusLabel: String?,
    val urgency: String?,
    val explanation: String?,
    val recommendation: String? = null,
    val speciesId: String? = null,
    val species: String? = null,
    val speciesDetails: PlantSpeciesDto? = null
)
