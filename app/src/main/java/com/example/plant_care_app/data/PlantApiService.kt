package com.example.plant_care_app.data

import com.example.plant_care_app.ui.models.CreateSensorRequest
import com.example.plant_care_app.ui.models.PlantDetailDto
import com.example.plant_care_app.ui.models.UpdatePlantSensorRequest
import com.example.plant_care_app.ui.models.PlantOverviewDto
import com.example.plant_care_app.ui.models.PlantSpeciesDto
import com.example.plant_care_app.ui.models.PlantStatusDto
import com.example.plant_care_app.ui.models.ReadingDto
import com.example.plant_care_app.ui.models.SensorDto
import com.example.plant_care_app.ui.models.UpdateSensorRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface PlantApiService {

    @GET("api/species")
    suspend fun getSpecies(): List<PlantSpeciesDto>

    @GET("api/species/{id}")
    suspend fun getSpeciesById(@Path("id") id: String): PlantSpeciesDto

    @GET("api/plants/overview")
    suspend fun getOverview(): List<PlantOverviewDto>

    @GET("api/plants/{id}")
    suspend fun getPlantById(@Path("id") id: String): PlantDetailDto

    @GET("api/plants/{plantId}/readings")
    suspend fun getReadings(@Path("plantId") plantId: String): List<ReadingDto>

    @GET("api/plants/{plantId}/status")
    suspend fun getPlantStatus(@Path("plantId") plantId: String): PlantStatusDto

    @PUT("api/plants/{id}")
    suspend fun updatePlantSensor(@Path("id") id: String, @Body body: UpdatePlantSensorRequest): PlantDetailDto


    @GET("api/sensors")
    suspend fun getSensors(): List<SensorDto>

    @GET("api/sensors/{id}")
    suspend fun getSensorById(@Path("id") id: String): SensorDto

    @POST("api/sensors")
    suspend fun createSensor(@Body body: CreateSensorRequest): SensorDto

    @PUT("api/sensors/{id}")
    suspend fun updateSensor(@Path("id") id: String, @Body body: UpdateSensorRequest): SensorDto

    @DELETE("api/sensors/{id}")
    suspend fun deleteSensor(@Path("id") id: String): SensorDto

    @Multipart
    @POST("api/plants")
    suspend fun createPlant(
        @Part image: MultipartBody.Part?,
        @Part("name") name: RequestBody,
        @Part("speciesId") speciesId: RequestBody,
        @Part("location") location: RequestBody,
        @Part("sensorId") sensorId: RequestBody?
    ): PlantDetailDto
}
