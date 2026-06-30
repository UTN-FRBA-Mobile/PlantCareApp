package com.example.plant_care_app.notifications

import android.util.Log
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.ui.models.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        
        // 1. Guardar localmente
        SessionManager.saveFcmToken(applicationContext, token)
        
        // 2. Si el usuario ya está autenticado, actualizar en el backend inmediatamente
        val authToken = SessionManager.getToken(applicationContext)
        if (authToken != null) {
            serviceScope.launch {
                try {
                    // Retrofit ya tiene el interceptor que añade el Bearer token
                    RetrofitClient.authApi.updateFcmToken(FcmTokenRequest(token))
                    Log.d("FCM", "Token actualizado en el servidor tras refresco")
                } catch (e: Exception) {
                    Log.e("FCM", "Error al actualizar token tras refresco", e)
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "From: ${message.from}")

        // Handle data payload
        if (message.data.isNotEmpty()) {
            val plantId = message.data["plantId"]
            val plantName = message.data["plantName"] ?: "Planta"
            val alertType = message.data["alertType"]

            val notificationHelper = NotificationHelper(applicationContext)
            val title = "$plantName necesita atención 🌱"
            val body = when (alertType) {
                "HIGH_STRESS" -> "Se ha detectado estrés alto en tu planta."
                else -> "Revisá el estado de tu planta."
            }

            notificationHelper.showNotification(title, body, plantId)
        }

        // Handle notification payload (if any, though data is preferred for background handling)
        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }
    }
}
