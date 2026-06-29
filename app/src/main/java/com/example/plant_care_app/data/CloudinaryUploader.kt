package com.example.plant_care_app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

/**
 * Sube imagenes directamente desde la app a Cloudinary (subida "unsigned").
 *
 * Por que directo desde la app y no via backend: el backend solo guarda un string
 * imageUrl; reenviarle el archivo para que el lo suba a Cloudinary seria un salto extra
 * de red sin valor. Subimos directo al storage y le pasamos la URL ya resuelta.
 *
 * Seguridad:
 * - CLOUD_NAME y UPLOAD_PRESET NO son secretos (aparecen en cualquier URL de Cloudinary);
 *   el api_secret nunca viaja en la app, por eso el preset es "unsigned".
 * - Usa su propio OkHttpClient, SIN el authInterceptor de RetrofitClient, para no
 *   filtrar el JWT del backend a un tercero.
 */
object CloudinaryUploader {

    private const val CLOUD_NAME = "dcaeqlthk"
    private const val UPLOAD_PRESET = "plantcare_uns"

    private const val UPLOAD_URL = "https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload"

    private val client = OkHttpClient()

    /** Sube el archivo y devuelve la secure_url (https) que sirve Cloudinary. */
    suspend fun upload(file: File): String = withContext(Dispatchers.IO) {
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("upload_preset", UPLOAD_PRESET)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("image/jpeg".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(UPLOAD_URL)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val json = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                error("Cloudinary respondio ${response.code}: $json")
            }
            JSONObject(json).getString("secure_url")
        }
    }
}
