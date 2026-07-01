package com.example.plant_care_app.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.ui.models.FcmTokenRequest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de integración para validar la registración del token FCM en el backend.
 * Nota: Requiere conectividad con el servidor de desarrollo.
 */
@RunWith(AndroidJUnit4::class)
class FcmTokenRegistrationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        RetrofitClient.init(context)
    }

    @Test
    fun testRegisterFcmToken_Success() = runBlocking {
        // Simular que el usuario está logueado para que la llamada no sea rechazada (si el backend requiere auth)
        // En un entorno de test real, deberías tener un usuario de prueba.
        val sessionToken = SessionManager.getToken(context)
        
        if (sessionToken != null) {
            val request = FcmTokenRequest(
                token = "test_fcm_token_123",
                deviceModel = "Android_Test_Device"
            )
            
            try {
                RetrofitClient.authApi.registerFcmToken(request)
                // Si no lanza excepción, asumimos éxito (200 OK)
                assert(true)
            } catch (e: Exception) {
                // Falló la llamada a la API
                throw e
            }
        } else {
            // No podemos probar sin login si el endpoint está protegido.
            // Opcionalmente, intentar login aquí o marcar como ignorado.
            println("Saltando test porque no hay sesión activa.")
        }
    }
}
