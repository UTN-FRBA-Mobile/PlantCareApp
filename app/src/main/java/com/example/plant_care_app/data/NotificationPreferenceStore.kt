package com.example.plant_care_app.data

import android.content.Context

/**
 * Guarda localmente el estado de las notificaciones de plantas.
 * Permite saber si una planta ya fue notificada por un estado determinado
 * y evita repetir la misma alerta cada vez que se abre la app.
 */
class NotificationPreferenceStore(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun shouldNotifyPlant(plantId: String, statusLabel: String): Boolean {
        return prefs.getString(statusKey(plantId), null) != statusLabel
    }

    fun markPlantAsNotified(plantId: String, statusLabel: String) {
        prefs.edit()
            .putString(statusKey(plantId), statusLabel)
            .putLong(notifiedAtKey(plantId), System.currentTimeMillis())
            .apply()
    }

    fun markPlantStatusObserved(plantId: String, statusLabel: String?) {
        prefs.edit()
            .putString(statusKey(plantId), statusLabel.orEmpty())
            .apply()
    }

    fun hasAskedNotificationPermission(): Boolean {
        return prefs.getBoolean(NOTIFICATION_PERMISSION_ASKED_KEY, false)
    }

    fun markNotificationPermissionAsked() {
        prefs.edit()
            .putBoolean(NOTIFICATION_PERMISSION_ASKED_KEY, true)
            .apply()
    }

    private fun statusKey(plantId: String) = "plant_${plantId}_notification_status"

    private fun notifiedAtKey(plantId: String) = "plant_${plantId}_notification_notified_at"

    private companion object {
        const val PREFS_NAME = "plant_care_notifications"
        const val NOTIFICATION_PERMISSION_ASKED_KEY = "notification_permission_asked"
    }
}
