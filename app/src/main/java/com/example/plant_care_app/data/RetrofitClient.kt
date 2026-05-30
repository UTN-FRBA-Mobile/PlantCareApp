package com.example.plant_care_app.data

import android.content.Context
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient

object RetrofitClient {

    private const val TAG = "RetrofitClient"

    // URL base del backend configurada por variante de build.
    val BASE_URL: String = AppConfig.BASE_URL

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
        Log.d(TAG, "Using BASE_URL: $BASE_URL")

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
