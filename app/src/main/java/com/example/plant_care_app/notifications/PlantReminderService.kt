package com.example.plant_care_app.notifications

import android.content.Context

/**
 * Service layer responsible for coordinating plant reminders.
 * This class acts as a bridge between data sources (Retrofit, Firebase, Workers)
 * and the low-level NotificationHelper.
 */
class PlantReminderService(context: Context) {

    private val notificationHelper = NotificationHelper(context)

    /**
     * Sends a reminder notification for a specific plant.
     *
     * @param plantName The name of the plant (e.g., "Monstera")
     * @param reminderMessage The action needed (e.g., "needs watering")
     */
    fun sendPlantReminder(plantName: String, reminderMessage: String) {
        // Here we could add additional logic, like logging or analytics
        notificationHelper.showPlantNotification(plantName, reminderMessage)
    }
}
