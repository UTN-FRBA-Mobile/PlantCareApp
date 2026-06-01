package com.example.plant_care_app.notifications

import com.example.plant_care_app.data.NotificationPreferenceStore
import com.example.plant_care_app.ui.models.PlantOverviewDto

/**
 * Decide qué alertas de plantas deben notificarse y registra las ya enviadas.
 */
class PlantAlertNotificationManager(
    private val notificationStore: NotificationPreferenceStore,
    private val plantReminderService: PlantReminderService
) {

    fun getPendingHighStressAlerts(plants: List<PlantOverviewDto>): List<PlantOverviewDto> {
        recordNonAlertStatuses(plants)

        return plants.filter { plant ->
            plant.statusLabel == HIGH_STRESS_STATUS &&
                notificationStore.shouldNotifyPlant(plant.id, HIGH_STRESS_STATUS)
        }
    }

    fun notifyHighStressPlants(plants: List<PlantOverviewDto>) {
        plants.forEach { plant ->
            val statusLabel = plant.statusLabel ?: return@forEach

            plantReminderService.sendPlantAlertNotification(
                plantName = plant.name,
                soilMoisture = plant.soilMoisture
            )
            notificationStore.markPlantAsNotified(
                plantId = plant.id,
                statusLabel = statusLabel
            )
        }
    }

    private fun recordNonAlertStatuses(plants: List<PlantOverviewDto>) {
        plants
            .filter { it.statusLabel != HIGH_STRESS_STATUS }
            .forEach { plant ->
                notificationStore.markPlantStatusObserved(
                    plantId = plant.id,
                    statusLabel = plant.statusLabel
                )
            }
    }

    private companion object {
        const val HIGH_STRESS_STATUS = "Estres alto"
    }
}
