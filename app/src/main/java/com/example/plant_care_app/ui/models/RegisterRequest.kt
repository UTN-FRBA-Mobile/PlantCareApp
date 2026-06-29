package com.example.plant_care_app.ui.models

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)