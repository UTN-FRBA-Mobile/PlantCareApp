package com.example.plant_care_app.notifications

import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.plant_care_app.data.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
        
        // 1. Guardar localmente
        SessionManager.saveFcmToken(applicationContext, token)
        
        // 2. Programar envío al backend usando WorkManager para asegurar entrega
        scheduleTokenUpload(token)
    }

    private fun scheduleTokenUpload(token: String) {
        val data = Data.Builder()
            .putString("token", token)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<FcmTokenWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "fcm_token_upload",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
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

        // Handle notification payload
        message.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }
    }
}
