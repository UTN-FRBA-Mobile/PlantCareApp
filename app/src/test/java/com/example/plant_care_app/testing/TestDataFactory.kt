package com.example.plant_care_app.testing

import com.example.plant_care_app.ui.models.AuthResponse
import com.example.plant_care_app.ui.models.PlantDetailDto
import com.example.plant_care_app.ui.models.PlantOverviewDto
import com.example.plant_care_app.ui.models.PlantSpeciesDto
import com.example.plant_care_app.ui.models.PlantSpeciesMessagesDto
import com.example.plant_care_app.ui.models.PlantStatusDto
import com.example.plant_care_app.ui.models.ReadingDto
import com.example.plant_care_app.ui.models.SensorDto
import com.example.plant_care_app.ui.models.UserDto

object TestDataFactory {

    fun user(
        id: String = "user-1",
        name: String = "Mora",
        email: String = "mora@example.com"
    ) = UserDto(
        id = id,
        name = name,
        email = email
    )

    fun authResponse(
        token: String = "test-token",
        user: UserDto = user()
    ) = AuthResponse(
        token = token,
        user = user
    )

    fun sensor(
        id: String = "sensor-1",
        name: String = "Sensor patio",
        status: String = "available",
        apiKey: String? = "api-key-1",
        userId: String? = "user-1",
        createdAt: String? = "2026-06-30T12:00:00Z"
    ) = SensorDto(
        id = id,
        name = name,
        status = status,
        apiKey = apiKey,
        userId = userId,
        createdAt = createdAt
    )

    fun plantSpecies(
        id: String = "species-1",
        displayName: String = "Monstera deliciosa",
        emoji: String? = null,
        imageUrl: String? = null,
        humidityMin: Int? = 40,
        humidityMax: Int? = 70,
        difficulty: String? = "media",
        wateringFrequency: String? = "Semanal",
        lightRequirement: String? = "Luz indirecta",
        temperatureMin: Int? = 18,
        temperatureMax: Int? = 28,
        description: String? = "Planta tropical de interior.",
        messages: PlantSpeciesMessagesDto? = PlantSpeciesMessagesDto(
            needsWater = "Necesita agua",
            optimal = "Humedad ideal",
            excessWater = "Exceso de agua"
        ),
        careTips: List<String>? = listOf("Evitar sol directo"),
        facts: List<String>? = listOf("Sus hojas pueden desarrollar fenestraciones"),
        aliases: List<String>? = listOf("Costilla de Adan")
    ) = PlantSpeciesDto(
        id = id,
        displayName = displayName,
        emoji = emoji,
        imageUrl = imageUrl,
        humidityMin = humidityMin,
        humidityMax = humidityMax,
        difficulty = difficulty,
        wateringFrequency = wateringFrequency,
        lightRequirement = lightRequirement,
        temperatureMin = temperatureMin,
        temperatureMax = temperatureMax,
        description = description,
        messages = messages,
        careTips = careTips,
        facts = facts,
        aliases = aliases
    )

    fun plantOverview(
        id: String = "plant-1",
        name: String = "Monstera",
        location: String = "Living",
        imageUrl: String? = "/uploads/plant-1.jpg",
        sensorId: String? = "sensor-1",
        sensorName: String? = "Sensor patio",
        hasSensor: Boolean = sensorId != null,
        soilMoisture: Int? = 52,
        readAt: String? = "2026-06-30T12:00:00Z",
        recommendation: String? = "Mantener humedad estable.",
        urgency: String? = "media",
        statusLabel: String? = "Optimo",
        speciesId: String? = "species-1",
        species: String? = "Monstera deliciosa",
        speciesDetails: PlantSpeciesDto? = plantSpecies()
    ) = PlantOverviewDto(
        id = id,
        name = name,
        location = location,
        imageUrl = imageUrl,
        sensorId = sensorId,
        sensorName = sensorName,
        hasSensor = hasSensor,
        soilMoisture = soilMoisture,
        readAt = readAt,
        recommendation = recommendation,
        urgency = urgency,
        statusLabel = statusLabel,
        speciesId = speciesId,
        species = species,
        speciesDetails = speciesDetails
    )

    fun plantDetail(
        id: String = "plant-1",
        name: String = "Monstera",
        species: String = "Monstera deliciosa",
        location: String = "Living",
        imageUrl: String? = "/uploads/plant-1.jpg",
        sensorId: String? = "sensor-1",
        lastWateringAt: String? = "2026-06-29T09:00:00Z",
        speciesId: String? = "species-1",
        speciesDetails: PlantSpeciesDto? = plantSpecies()
    ) = PlantDetailDto(
        id = id,
        name = name,
        species = species,
        location = location,
        imageUrl = imageUrl,
        sensorId = sensorId,
        lastWateringAt = lastWateringAt,
        speciesId = speciesId,
        speciesDetails = speciesDetails
    )

    fun reading(
        id: String = "reading-1",
        sensorId: String = "sensor-1",
        soilMoisture: Int = 52,
        readAt: String? = "2026-06-30T12:00:00Z",
        source: String? = "sensor",
        stressStatus: String? = "Optimo",
        explanation: String? = "La humedad esta dentro del rango esperado.",
        recommendation: String? = "Mantener rutina actual."
    ) = ReadingDto(
        id = id,
        sensorId = sensorId,
        soilMoisture = soilMoisture,
        readAt = readAt,
        source = source,
        stressStatus = stressStatus,
        explanation = explanation,
        recommendation = recommendation
    )

    fun plantStatus(
        statusLabel: String? = "Optimo",
        urgency: String? = "baja",
        explanation: String? = "La planta esta en buen estado.",
        recommendation: String? = "Mantener rutina actual.",
        speciesId: String? = "species-1",
        species: String? = "Monstera deliciosa",
        speciesDetails: PlantSpeciesDto? = plantSpecies()
    ) = PlantStatusDto(
        statusLabel = statusLabel,
        urgency = urgency,
        explanation = explanation,
        recommendation = recommendation,
        speciesId = speciesId,
        species = species,
        speciesDetails = speciesDetails
    )
}
