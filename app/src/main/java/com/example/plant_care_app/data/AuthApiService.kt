package com.example.plant_care_app.data

import com.example.plant_care_app.ui.models.AuthResponse
import com.example.plant_care_app.ui.models.LoginRequest
import com.example.plant_care_app.ui.models.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
}