package com.example.plant_care_app.notifications

import com.example.plant_care_app.data.NotificationPreferenceStore
import com.example.plant_care_app.testing.TestDataFactory
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test

class PlantAlertNotificationManagerTest {

    private val notificationStore = mockk<NotificationPreferenceStore>(relaxed = true)
    private val plantReminderService = mockk<PlantReminderService>(relaxed = true)
    private val manager = PlantAlertNotificationManager(
        notificationStore = notificationStore,
        plantReminderService = plantReminderService
    )

    @Test
    fun `getPendingHighStressAlerts returns only high stress plants that should notify`() {
        val pendingPlant = TestDataFactory.plantOverview(
            id = "plant-pending",
            statusLabel = HIGH_STRESS_STATUS
        )
        val alreadyNotifiedPlant = TestDataFactory.plantOverview(
            id = "plant-notified",
            statusLabel = HIGH_STRESS_STATUS
        )
        val healthyPlant = TestDataFactory.plantOverview(
            id = "plant-healthy",
            statusLabel = "Optimo"
        )

        every {
            notificationStore.shouldNotifyPlant("plant-pending", HIGH_STRESS_STATUS)
        } returns true
        every {
            notificationStore.shouldNotifyPlant("plant-notified", HIGH_STRESS_STATUS)
        } returns false

        val result = manager.getPendingHighStressAlerts(
            listOf(pendingPlant, alreadyNotifiedPlant, healthyPlant)
        )

        assertEquals(listOf(pendingPlant), result)
        verify(exactly = 0) {
            notificationStore.shouldNotifyPlant("plant-healthy", HIGH_STRESS_STATUS)
        }
    }

    @Test
    fun `getPendingHighStressAlerts marks non alert plant statuses as observed`() {
        val highStressPlant = TestDataFactory.plantOverview(
            id = "plant-high-stress",
            statusLabel = HIGH_STRESS_STATUS
        )
        val optimalPlant = TestDataFactory.plantOverview(
            id = "plant-optimal",
            statusLabel = "Optimo"
        )
        val unknownStatusPlant = TestDataFactory.plantOverview(
            id = "plant-unknown",
            statusLabel = null
        )

        every {
            notificationStore.shouldNotifyPlant("plant-high-stress", HIGH_STRESS_STATUS)
        } returns true

        manager.getPendingHighStressAlerts(
            listOf(highStressPlant, optimalPlant, unknownStatusPlant)
        )

        verify {
            notificationStore.markPlantStatusObserved("plant-optimal", "Optimo")
            notificationStore.markPlantStatusObserved("plant-unknown", null)
        }
        verify(exactly = 0) {
            notificationStore.markPlantStatusObserved("plant-high-stress", any())
        }
    }

    @Test
    fun `notifyHighStressPlants sends notification and marks plant as notified`() {
        val plant = TestDataFactory.plantOverview(
            id = "plant-alert",
            name = "Helecho",
            soilMoisture = 18,
            statusLabel = HIGH_STRESS_STATUS
        )

        every {
            plantReminderService.sendPlantAlertNotification(any(), any())
        } just runs
        every {
            notificationStore.markPlantAsNotified(any(), any())
        } just runs

        manager.notifyHighStressPlants(listOf(plant))

        verify(exactly = 1) {
            plantReminderService.sendPlantAlertNotification(
                plantName = "Helecho",
                soilMoisture = 18
            )
            notificationStore.markPlantAsNotified(
                plantId = "plant-alert",
                statusLabel = HIGH_STRESS_STATUS
            )
        }
    }

    @Test
    fun `notifyHighStressPlants skips plants without status label`() {
        val plant = TestDataFactory.plantOverview(
            id = "plant-without-status",
            statusLabel = null
        )

        manager.notifyHighStressPlants(listOf(plant))

        verify(exactly = 0) {
            plantReminderService.sendPlantAlertNotification(any(), any())
            notificationStore.markPlantAsNotified(any(), any())
        }
    }

    private companion object {
        const val HIGH_STRESS_STATUS = "Estres alto"
    }
}
