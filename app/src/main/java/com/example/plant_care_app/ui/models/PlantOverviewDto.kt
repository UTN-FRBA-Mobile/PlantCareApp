package com.example.plant_care_app.ui.models

data class PlantOverviewDto(
    val id: String,
    val name: String,
    val location: String,
    val imageUrl: String?,
    val sensorId: String?,
    val sensorName: String?,
    val hasSensor: Boolean,
    val soilMoisture: Int?,
    val readAt: String?,
    val recommendation: String?,
    val urgency: String?,
    val statusLabel: String?
)