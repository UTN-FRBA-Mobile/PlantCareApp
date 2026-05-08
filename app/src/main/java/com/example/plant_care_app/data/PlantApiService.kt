package com.example.plant_care_app.data

import com.example.plant_care_app.ui.models.PlantOverviewDto
import retrofit2.http.GET

interface PlantApiService {

    @GET("/api/plants/overview")
    suspend fun getOverview(): List<PlantOverviewDto>
}