package com.example.plant_care_app.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionManagerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        SessionManager.clearToken(context)
    }

    @After
    fun tearDown() {
        SessionManager.clearToken(context)
    }

    @Test
    fun saveTokenStoresToken() {
        SessionManager.saveToken(context, "token-123")

        val result = SessionManager.getToken(context)

        assertEquals("token-123", result)
    }

    @Test
    fun clearTokenRemovesStoredToken() {
        SessionManager.saveToken(context, "token-123")

        SessionManager.clearToken(context)

        assertNull(SessionManager.getToken(context))
    }

    @Test
    fun getTokenReturnsNullWhenNoTokenWasSaved() {
        val result = SessionManager.getToken(context)

        assertNull(result)
    }
}
