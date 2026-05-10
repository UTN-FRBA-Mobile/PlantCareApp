package com.example.plant_care_app.ui.models

data class ReadingDto(
    val id: String,
    val sensorId: String,
    val soilMoisture: Int,
    val readAt: String?,
    val source: String?
)
