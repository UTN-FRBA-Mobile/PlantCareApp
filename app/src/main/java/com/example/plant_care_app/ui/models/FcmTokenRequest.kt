package com.example.plant_care_app.ui.models

data class FcmTokenRequest(
    val token: String,
    val deviceModel: String,
    val platform: String = "android"
)
