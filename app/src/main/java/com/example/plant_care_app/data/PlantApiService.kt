package com.example.plant_care_app.data

import com.example.plant_care_app.ui.models.PlantDetailDto
import com.example.plant_care_app.ui.models.PlantOverviewDto
import com.example.plant_care_app.ui.models.PlantStatusDto
import com.example.plant_care_app.ui.models.ReadingDto
import com.example.plant_care_app.ui.models.SensorDto
import retrofit2.http.GET
import retrofit2.http.Path

interface PlantApiService {

    @GET("/api/plants/overview")
    suspend fun getOverview(): List<PlantOverviewDto>

    @GET("/api/plants/{id}")
    suspend fun getPlantById(@Path("id") id: String): PlantDetailDto

    @GET("/api/plants/{plantId}/readings")
    suspend fun getReadings(@Path("plantId") plantId: String): List<ReadingDto>

    @GET("/api/plants/{plantId}/status")
    suspend fun getPlantStatus(@Path("plantId") plantId: String): PlantStatusDto


    @GET("/api/sensors")
    suspend fun getSensors(): List<SensorDto>
}
