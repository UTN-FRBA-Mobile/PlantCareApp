package com.example.plant_care_app.ui.models

data class AuthResponse(
    val token: String,
    val user: UserDto
)