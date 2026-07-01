package com.example.plant_care_app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationPreferenceStoreTest {

    private lateinit var context: Context
    private lateinit var store: NotificationPreferenceStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        clearNotificationPrefs()
        store = NotificationPreferenceStore(context)
    }

    @After
    fun tearDown() {
        clearNotificationPrefs()
    }

    @Test
    fun shouldNotifyPlantReturnsTrueWhenPlantWasNotNotifiedForStatus() {
        val result = store.shouldNotifyPlant(
            plantId = "notification-plant-1",
            statusLabel = "Estres alto"
        )

        assertTrue(result)
    }

    @Test
    fun shouldNotifyPlantReturnsFalseAfterPlantWasMarkedAsNotifiedForSameStatus() {
        store.markPlantAsNotified(
            plantId = "notification-plant-1",
            statusLabel = "Estres alto"
        )

        val result = store.shouldNotifyPlant(
            plantId = "notification-plant-1",
            statusLabel = "Estres alto"
        )

        assertFalse(result)
    }

    @Test
    fun shouldNotifyPlantReturnsTrueWhenPlantStatusChangedAfterNotification() {
        store.markPlantAsNotified(
            plantId = "notification-plant-1",
            statusLabel = "Estres alto"
        )

        val result = store.shouldNotifyPlant(
            plantId = "notification-plant-1",
            statusLabel = "Optimo"
        )

        assertTrue(result)
    }

    @Test
    fun markPlantStatusObservedPreventsNotificationForObservedStatus() {
        store.markPlantStatusObserved(
            plantId = "notification-plant-1",
            statusLabel = "Optimo"
        )

        val result = store.shouldNotifyPlant(
            plantId = "notification-plant-1",
            statusLabel = "Optimo"
        )

        assertFalse(result)
    }

    @Test
    fun markPlantStatusObservedWithNullStoresEmptyStatus() {
        store.markPlantStatusObserved(
            plantId = "notification-plant-1",
            statusLabel = null
        )

        val result = store.shouldNotifyPlant(
            plantId = "notification-plant-1",
            statusLabel = ""
        )

        assertFalse(result)
    }

    @Test
    fun notificationPermissionStartsAsNotAskedAndCanBeMarkedAsAsked() {
        assertFalse(store.hasAskedNotificationPermission())

        store.markNotificationPermissionAsked()

        assertTrue(store.hasAskedNotificationPermission())
    }

    private fun clearNotificationPrefs() {
        context.getSharedPreferences("plant_care_notifications", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }
}
