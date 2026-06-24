package com.example.plant_care_app.ui.models

data class GardenerAdviceRequest(
    val userMessage: String?
)

data class GardenerAdviceResponse(
    val plantId: String,
    val advice: String
)
