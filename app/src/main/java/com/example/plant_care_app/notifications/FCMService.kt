package com.example.plant_care_app.notifications

import android.os.Build
import android.util.Log
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.ui.models.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FCMService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "Nuevo token generado: $token")
        sendTokenToBackend(token)
    }

    private fun sendTokenToBackend(token: String) {
        val request = FcmTokenRequest(
            token = token,
            deviceModel = Build.MODEL
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Solo intentamos registrar si el usuario tiene sesión activa
                if (SessionManager.getToken(applicationContext) != null) {
                    RetrofitClient.authApi.registerFcmToken(request)
                    Log.i("FCMService", "FCM Token actualizado en el backend con éxito")
                }
            } catch (e: Exception) {
                Log.e("FCMService", "Error al actualizar FCM Token en el backend: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Manejo exclusivo de mensajes tipo "Data" según lo solicitado
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Plant Care"
            val message = remoteMessage.data["message"] ?: ""
            
            // Reutilizamos la infraestructura de notificaciones existente
            NotificationHelper(applicationContext).showNotification(title, message)
        }
    }
}
