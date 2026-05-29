package com.example.plant_care_app.data

import com.example.plant_care_app.ui.models.PlantDetailDto
import com.example.plant_care_app.ui.models.PlantOverviewDto
import com.example.plant_care_app.ui.models.PlantStatusDto
import com.example.plant_care_app.ui.models.ReadingDto
import com.example.plant_care_app.ui.models.SensorDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface PlantApiService {

    @GET("api/plants/overview")
    suspend fun getOverview(): List<PlantOverviewDto>

    @GET("api/plants/{id}")
    suspend fun getPlantById(@Path("id") id: String): PlantDetailDto

    @GET("api/plants/{plantId}/readings")
    suspend fun getReadings(@Path("plantId") plantId: String): List<ReadingDto>

    @GET("api/plants/{plantId}/status")
    suspend fun getPlantStatus(@Path("plantId") plantId: String): PlantStatusDto


    @GET("api/sensors")
    suspend fun getSensors(): List<SensorDto>

    @Multipart
    @POST("api/plants")
    suspend fun createPlant(
        @Part image: MultipartBody.Part?,
        @Part("name") name: RequestBody,
        @Part("species") species: RequestBody,
        @Part("location") location: RequestBody,
        @Part("sensorId") sensorId: RequestBody?
    ): PlantDetailDto
}
