package com.example.plant_care_app.data

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

/**
 * Convierte una excepcion de red en un mensaje legible para el usuario.
 *
 * Para los errores de validacion del backend (HTTP 400) arma el detalle por campo
 * (ej. "Contraseña: debe tener como máximo 8 caracteres") en lugar del generico
 * "HTTP 400 Bad Request" que trae HttpException.message.
 *
 * El backend responde con este shape:
 * {"success":false,"error":{"message":"Validation failed",
 *   "details":{"fieldErrors":{"password":["Password must be at most 8 characters"]}}}}
 */
fun Throwable.toUserMessage(): String = when (this) {
    is HttpException -> parseHttpError(this)
    is IOException -> "Sin conexión. Revisá tu internet e intentá de nuevo."
    else -> message ?: "Ocurrió un error inesperado."
}

/**
 * Mensaje para la pantalla de login. A diferencia de [toUserMessage], NO expone el
 * detalle de validación por campo (ej. "la contraseña debe tener al menos 4 caracteres"):
 * en el login eso no le sirve al usuario, cualquier 4xx significa que las credenciales
 * simplemente no son válidas.
 */
fun Throwable.toLoginMessage(): String = when (this) {
    is HttpException -> when (code()) {
        in 400..499 -> "Correo o contraseña incorrectos"
        in 500..599 -> "Error del servidor. Intentá de nuevo más tarde."
        else -> "Error ${code()}."
    }
    is IOException -> "Sin conexión. Revisá tu internet e intentá de nuevo."
    else -> message ?: "Ocurrió un error inesperado."
}

// Etiquetas en español para los campos que devuelve el backend.
private val fieldLabels = mapOf(
    "name" to "Nombre",
    "email" to "Correo electrónico",
    "password" to "Contraseña"
)

private fun parseHttpError(e: HttpException): String {
    // errorBody().string() se consume una sola vez; lo leemos a una variable local.
    val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        ?: return "Error ${e.code()}."

    val parsed = runCatching {
        val error = JSONObject(body).getJSONObject("error")
        val fieldErrors = error.optJSONObject("details")?.optJSONObject("fieldErrors")

        if (fieldErrors != null && fieldErrors.length() > 0) {
            // Un renglón por cada campo inválido.
            fieldErrors.keys().asSequence().map { field ->
                val label = fieldLabels[field] ?: field
                val firstMsg = fieldErrors.getJSONArray(field).optString(0)
                "• $label: ${translateRule(firstMsg)}"
            }.joinToString("\n")
        } else {
            // Sin detalle por campo (ej. credenciales inválidas): traducimos el mensaje general.
            val general = error.optString("message")
            if (general.isBlank()) "Error ${e.code()}." else translateRule(general)
        }
    }.getOrNull()

    return parsed ?: "Error ${e.code()}."
}

// Traduce los mensajes de validacion conocidos; si no lo reconoce, deja el original del backend.
private fun translateRule(msg: String): String = when {
    msg.contains("Invalid email or password", ignoreCase = true) ||
        msg.contains("Invalid credentials", ignoreCase = true) -> "Correo o contraseña incorrectos"
    msg.equals("Required", ignoreCase = true) -> "es obligatorio"
    msg.contains("Invalid email", ignoreCase = true) -> "no tiene un formato válido"
    msg.contains("at least", ignoreCase = true) ->
        Regex("\\d+").find(msg)?.value?.let { "debe tener al menos $it caracteres" } ?: msg
    msg.contains("at most", ignoreCase = true) ->
        Regex("\\d+").find(msg)?.value?.let { "debe tener como máximo $it caracteres" } ?: msg
    else -> msg
}
