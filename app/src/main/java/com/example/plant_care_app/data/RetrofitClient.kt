package com.example.plant_care_app.data

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object RetrofitClient {

    // URL base del backend
    //private const val BASE_URL = "https://backend-riego-inteligente.onrender.com/"
    private const val BASE_URL = "https://backend-plantcare-dev.onrender.com/"
    //const val BASE_URL = "http://10.0.2.2:3000/"

    // Android real en tu Wi-Fi:
    //const val BASE_URL = "http://192.168.100.3:3000/"

    private lateinit var appContext: Context

    // Guarda el contexto de la app para poder leer la sesion desde el interceptor de OkHttp.
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Agrega el token JWT a cada request cuando el usuario ya inicio sesion.
    private val authInterceptor = Interceptor { chain ->
        val token = SessionManager.getToken(appContext)

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    // Instancia unica de Retrofit para compartir configuracion, base URL y autenticacion.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val plantApi: PlantApiService by lazy {
        retrofit.create(PlantApiService::class.java)
    }

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
}