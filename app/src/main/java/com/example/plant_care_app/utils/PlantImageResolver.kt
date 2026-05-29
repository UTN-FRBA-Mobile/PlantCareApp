package com.example.plant_care_app.utils

import android.content.Context
import com.example.plant_care_app.data.PlantImageStore
import com.example.plant_care_app.data.RetrofitClient
import java.io.File

object PlantImageResolver {

    /**
     * Devuelve el modelo de imagen que usa la UI para mostrar una planta.
     *
     * Orden de prioridad:
     * 1. Usa el archivo local recibido por pantalla, cuando existe.
     * 2. Si no se recibio, busca una foto local guardada en PlantImageStore para esa planta.
     * 3. Si no hay foto local, usa imageUrl del backend: URL completa o ruta relativa con BASE_URL.
     * 4. Si nada aplica, devuelve null para que la UI muestre R.drawable.planta.
     *
     * El retorno es Any? porque la UI puede recibir un File local, una URL String o null.
     */
    fun resolve(
        context: Context,
        plantId: String,
        imageUrl: String?,
        localImagePath: String? = null
    ): Any? {
        val localFile = localImagePath
            ?.let { File(it) }
            ?.takeIf { it.exists() }
            ?: PlantImageStore.getImagePath(context, plantId)
                ?.let { File(it) }
                ?.takeIf { it.exists() }

        if (localFile != null) {
            return localFile
        }

        val remoteImageUrl = imageUrl
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return null

        return when {
            remoteImageUrl.startsWith("http://") ||
                remoteImageUrl.startsWith("https://") -> remoteImageUrl

            remoteImageUrl.startsWith("/") ->
                RetrofitClient.BASE_URL.trimEnd('/') + remoteImageUrl

            else -> null
        }
    }
}