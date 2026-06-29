package com.example.plant_care_app.ui.models

data class SensorDto(
    val id: String,
    val name: String,
    val status: String,
    val apiKey: String? = null,
    val userId: String? = null,
    val createdAt: String? = null,
)
