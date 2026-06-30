package com.example.plant_care_app.notifications

import android.content.Context

/**
 * Construye las notificaciones relacionadas al cuidado de plantas.
 * Traduce una alerta de planta en un título y mensaje listos para mostrar
 * usando NotificationHelper.
 */
class PlantReminderService(context: Context) {

    private val notificationHelper = NotificationHelper(context)

    fun sendPlantAlertNotification(
        plantId: String,
        plantName: String,
        soilMoisture: Int?
    ) {
        val message = if (soilMoisture != null) {
            "Humedad actual: $soilMoisture%. Revisá el estado de tu planta."
        } else {
            "Revisá el estado de tu planta."
        }

        notificationHelper.showNotification(
            title = "$plantName necesita atención \uD83C\uDF31",
            message = message,
            plantId = plantId
        )
    }
}
