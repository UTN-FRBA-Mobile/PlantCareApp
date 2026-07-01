package com.example.plant_care_app.data

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ApiErrorTest {

    @Test
    fun `toUserMessage returns connection message for IOExceptions`() {
        val error = IOException("timeout")

        val message = error.toUserMessage()

        assertEquals("Sin conexión. Revisá tu internet e intentá de nuevo.", message)
    }

    @Test
    fun `toLoginMessage returns invalid credentials for HTTP 400`() {
        val error = httpException(
            code = 400,
            body = """
                {
                  "success": false,
                  "error": {
                    "message": "Invalid email or password"
                  }
                }
            """.trimIndent()
        )

        val message = error.toLoginMessage()

        assertEquals("Correo o contraseña incorrectos", message)
    }

    @Test
    fun `toLoginMessage returns invalid credentials for HTTP 401`() {
        val error = httpException(
            code = 401,
            body = """
                {
                  "success": false,
                  "error": {
                    "message": "Unauthorized"
                  }
                }
            """.trimIndent()
        )

        val message = error.toLoginMessage()

        assertEquals("Correo o contraseña incorrectos", message)
    }

    @Test
    fun `toLoginMessage returns server message for HTTP 500`() {
        val error = httpException(
            code = 500,
            body = """
                {
                  "success": false,
                  "error": {
                    "message": "Internal server error"
                  }
                }
            """.trimIndent()
        )

        val message = error.toLoginMessage()

        assertEquals("Error del servidor. Intentá de nuevo más tarde.", message)
    }

    @Test
    fun `toUserMessage translates backend validation field errors`() {
        val error = httpException(
            code = 400,
            body = """
                {
                  "success": false,
                  "error": {
                    "message": "Validation failed",
                    "details": {
                      "fieldErrors": {
                        "email": ["Invalid email"],
                        "password": ["Password must be at least 8 characters"]
                      }
                    }
                  }
                }
            """.trimIndent()
        )

        val message = error.toUserMessage()

        val lines = message.lines().toSet()

        assertEquals(2, lines.size)
        assertTrue(lines.contains("• Correo electrónico: no tiene un formato válido"))
        assertTrue(lines.contains("• Contraseña: debe tener al menos 8 caracteres"))
    }

    @Test
    fun `toUserMessage translates backend general credential error`() {
        val error = httpException(
            code = 400,
            body = """
                {
                  "success": false,
                  "error": {
                    "message": "Invalid credentials"
                  }
                }
            """.trimIndent()
        )

        val message = error.toUserMessage()

        assertEquals("Correo o contraseña incorrectos", message)
    }

    @Test
    fun `toUserMessage returns generic code message for invalid JSON body`() {
        val error = httpException(code = 400, body = "not-json")

        val message = error.toUserMessage()

        assertEquals("Error 400.", message)
    }

    @Test
    fun `toUserMessage returns generic code message for empty body`() {
        val error = httpException(code = 422, body = "")

        val message = error.toUserMessage()

        assertEquals("Error 422.", message)
    }

    private fun httpException(code: Int, body: String): HttpException {
        val responseBody = body.toResponseBody("application/json".toMediaType())
        return HttpException(Response.error<Unit>(code, responseBody))
    }
}
