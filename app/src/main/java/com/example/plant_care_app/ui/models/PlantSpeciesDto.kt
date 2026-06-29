package com.example.plant_care_app.ui.models

data class PlantSpeciesDto(
    val id: String,
    val displayName: String,
    val emoji: String? = null,
    val imageUrl: String? = null,
    val humidityMin: Int? = null,
    val humidityMax: Int? = null,
    val difficulty: String? = null,
    val wateringFrequency: String? = null,
    val lightRequirement: String? = null,
    val temperatureMin: Int? = null,
    val temperatureMax: Int? = null,
    val description: String? = null,
    val messages: PlantSpeciesMessagesDto? = null,
    val careTips: List<String>? = null,
    val facts: List<String>? = null,
    val aliases: List<String>? = null
)

data class PlantSpeciesMessagesDto(
    val needsWater: String? = null,
    val optimal: String? = null,
    val excessWater: String? = null
)

// Respuesta del endpoint de identificación por imagen.
data class PlantIdentificationResponseDto(
    val bestMatch: PlantIdentificationCandidateDto? = null,
    val candidates: List<PlantIdentificationCandidateDto> = emptyList()
)

// Candidato devuelto por PlantNet con su nombre y nivel de confianza.
data class PlantIdentificationCandidateDto(
    val scientificName: String,
    val commonName: String? = null,
    val score: Double? = null,
    val matchedSpeciesId: String? = null,
    val displayName: String? = null
)
