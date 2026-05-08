package com.example.plant_care_app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue
import kotlin.jvm.java

object RetrofitClient {

    private const val BASE_URL = "https://backend-riego-inteligente.onrender.com/"
    //private const val BASE_URL = "http://10.0.2.2:3000/"

    val plantApi: PlantApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlantApiService::class.java)
    }
}