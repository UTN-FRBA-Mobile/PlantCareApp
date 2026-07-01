package com.example.plant_care_app.ui.components

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlantCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun plantCardDisplaysPlantInformation() {
        setPlantCard(
            name = "Monstera",
            location = "Living",
            humidity = 52,
            status = "Estres moderado",
            sensorName = "Sensor patio",
            hasSensor = true
        )

        composeTestRule.onNodeWithText("Monstera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Living").assertIsDisplayed()
        composeTestRule.onNodeWithText("52%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estres moderado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sensor patio").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Planta").assertIsDisplayed()
    }

    @Test
    fun plantCardDisplaysWithoutSensorTextWhenPlantHasNoSensor() {
        setPlantCard(
            sensorName = null,
            hasSensor = false
        )

        composeTestRule.onNodeWithText("Sin sensor").assertIsDisplayed()
    }

    @Test
    fun plantCardDisplaysFallbackSensorTextWhenSensorNameIsMissing() {
        setPlantCard(
            sensorName = null,
            hasSensor = true
        )

        composeTestRule.onNodeWithText("Sensor conectado").assertIsDisplayed()
    }

    @Test
    fun plantCardCallsOnClickWhenClicked() {
        var clickCount = 0
        setPlantCard(
            name = "Monstera",
            onClick = { clickCount += 1 }
        )

        composeTestRule
            .onNode(hasText("Monstera") and hasClickAction())
            .performClick()

        assertEquals(1, clickCount)
    }

    @Test
    fun plantCardCoercesHumidityProgressToValidRange() {
        setPlantCard(humidity = 150)

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(1f, 0f..1f, 0)))
            .assertIsDisplayed()
    }

    private fun setPlantCard(
        plantId: String = "plant-card-test",
        name: String = "Pothos",
        location: String = "Cocina",
        humidity: Int = 43,
        status: String = "Saludable",
        imageUrl: String? = null,
        sensorName: String? = "Sensor cocina",
        hasSensor: Boolean = true,
        onClick: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            PlantCareAppTheme {
                PlantCard(
                    plantId = plantId,
                    name = name,
                    location = location,
                    humidity = humidity,
                    status = status,
                    imageUrl = imageUrl,
                    sensorName = sensorName,
                    hasSensor = hasSensor,
                    onClick = onClick
                )
            }
        }
    }
}
