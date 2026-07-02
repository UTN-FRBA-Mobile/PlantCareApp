package com.example.plant_care_app.ui.screens

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.swipeUp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.plant_care_app.data.PlantApiService
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.models.GardenerAdviceRequest
import com.example.plant_care_app.ui.models.GardenerAdviceResponse
import com.example.plant_care_app.ui.models.PlantDetailDto
import com.example.plant_care_app.ui.models.PlantSpeciesDto
import com.example.plant_care_app.ui.models.PlantStatusDto
import com.example.plant_care_app.ui.models.ReadingDto
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Rule
import org.junit.Test

class PlantDetailScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var plantApi: PlantApiService

    @After
    fun tearDown() {
        unmockkObject(RetrofitClient)
    }

    @Test
    fun plantDetailDisplaysPlantStatusSpeciesAndReadings() {
        givenPlantDetailData()

        setPlantDetailScreen()
        waitForText("Monstera")

        composeTestRule.onNodeWithText("Monstera").assertIsDisplayed()
        composeTestRule.onNodeWithText("📍  Living").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estado actual").assertIsDisplayed()
        composeTestRule.onNodeWithText("Estres moderado").assertIsDisplayed()
        composeTestRule.onNodeWithText("55%").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monstera deliciosa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Humedad").assertIsDisplayed()
        scrollUntilTextIsDisplayed("Historial de mediciones")
        scrollUntilTextIsDisplayed("2026-07-01")
    }

    @Test
    fun plantDetailNavigatesToEvaluations() {
        givenPlantDetailData()

        setPlantDetailNavHost()
        waitForText("Monstera")

        scrollUntilTextIsDisplayed("Evaluaciones")
        composeTestRule
            .onNodeWithText("Evaluaciones")
            .performTouchInput { click(center) }

        composeTestRule
            .onNodeWithText("Evaluations plant-1 Monstera Monstera deliciosa")
            .assertIsDisplayed()
    }

    @Test
    fun plantDetailConsultsVirtualGardenerAndDisplaysResponse() {
        givenPlantDetailData()
        coEvery { plantApi.getGardenerAdvice(plantId = "plant-1", body = any()) } returns GardenerAdviceResponse(
            plantId = "plant-1",
            advice = "Revisá la humedad y evitá exceso de agua."
        )

        setPlantDetailScreen()
        waitForText("Monstera")
        scrollUntilTextIsDisplayed("Consultar")

        composeTestRule
            .onAllNodes(hasSetTextAction())[0]
            .performTextInput("Las hojas estan amarillas")
        composeTestRule
            .onNodeWithText("Consultar")
            .performClick()

        waitForText("Respuesta de Jardi")
        composeTestRule.onNodeWithText("Respuesta de Jardi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Revisá la humedad y evitá exceso de agua.").assertIsDisplayed()
        coVerify(exactly = 1) {
            plantApi.getGardenerAdvice(plantId = "plant-1", body = any())
        }
    }

    @Test
    fun plantEvaluationDisplaysReadingsHistory() {
        givenPlantDetailData()

        setEvaluationScreen()
        waitForText("Evaluaciones")

        composeTestRule.onNodeWithText("Monstera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monstera deliciosa").assertIsDisplayed()
        composeTestRule.onNodeWithText("Evaluaciones").assertIsDisplayed()
        composeTestRule.onNodeWithText("Historial de Evaluaciones").assertIsDisplayed()
        composeTestRule.onNodeWithText("01/07/2026, 10:00 - 55% - ").assertIsDisplayed()
        composeTestRule.onNodeWithText("OK").assertIsDisplayed()
        composeTestRule.onNodeWithText("30/06/2026, 10:00 - 20% - ").assertIsDisplayed()
        composeTestRule.onNodeWithText("REGAR").assertIsDisplayed()
    }

    @Test
    fun plantEvaluationDisplaysEmptyReadingsMessage() {
        givenPlantDetailData(readings = emptyList())

        setEvaluationScreen()
        waitForText("Aún no hay lecturas disponibles para esta planta.")

        composeTestRule
            .onNodeWithText("Aún no hay lecturas disponibles para esta planta.")
            .assertIsDisplayed()
    }

    @Test
    fun plantEvaluationDisplaysErrorWhenLoadingFails() {
        setUpApiMock()
        coEvery { plantApi.getPlantById("plant-1") } throws RuntimeException("boom")

        setEvaluationScreen()
        waitForText("Error al cargar las evaluaciones")

        composeTestRule
            .onNodeWithText("Error al cargar las evaluaciones")
            .assertIsDisplayed()
    }

    private fun setPlantDetailScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()

            PlantCareAppTheme {
                PlantDetailScreen(plantId = "plant-1", navController = navController)
            }
        }
    }

    private fun setPlantDetailNavHost() {
        composeTestRule.setContent {
            val navController = rememberNavController()

            PlantCareAppTheme {
                NavHost(navController = navController, startDestination = "detail") {
                    composable("detail") {
                        PlantDetailScreen(plantId = "plant-1", navController = navController)
                    }
                    composable("plant_evaluations/{plantId}/{name}/{type}") { backStackEntry ->
                        Text(
                            "Evaluations " +
                                "${backStackEntry.arguments?.getString("plantId")} " +
                                "${backStackEntry.arguments?.getString("name")} " +
                                "${backStackEntry.arguments?.getString("type")}"
                        )
                    }
                }
            }
        }
    }

    private fun setEvaluationScreen() {
        composeTestRule.setContent {
            val navController = rememberNavController()

            PlantCareAppTheme {
                PlantDetailEvaluationScreen(
                    navController = navController,
                    plantId = "plant-1",
                    plantName = "Fallback",
                    plantType = "Fallback type"
                )
            }
        }
    }

    private fun givenPlantDetailData(
        readings: List<ReadingDto> = defaultReadings()
    ) {
        setUpApiMock()
        coEvery { plantApi.getPlantById("plant-1") } returns plantDetail()
        coEvery { plantApi.getReadings("plant-1") } returns readings
        coEvery { plantApi.getPlantStatus("plant-1") } returns plantStatus()
    }

    private fun setUpApiMock() {
        plantApi = mockk()
        mockkObject(RetrofitClient)
        every { RetrofitClient.plantApi } returns plantApi
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasText(text))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun scrollUntilTextIsDisplayed(text: String, maxSwipes: Int = 8) {
        repeat(maxSwipes) {
            val isDisplayed = runCatching {
                composeTestRule.onNodeWithText(text).assertIsDisplayed()
            }.isSuccess

            if (isDisplayed) {
                return
            }

            composeTestRule.onRoot().performTouchInput { swipeUp() }
            composeTestRule.waitForIdle()
        }

        waitForText(text)
    }

    private fun plantDetail() = PlantDetailDto(
        id = "plant-1",
        name = "Monstera",
        species = "Monstera deliciosa",
        location = "Living",
        imageUrl = null,
        sensorId = "sensor-1",
        lastWateringAt = null,
        speciesId = "species-1",
        speciesDetails = PlantSpeciesDto(
            id = "species-1",
            displayName = "Monstera deliciosa",
            humidityMin = 40,
            humidityMax = 70,
            difficulty = "Media",
            wateringFrequency = "Semanal",
            lightRequirement = "Luz indirecta",
            temperatureMin = 18,
            temperatureMax = 28,
            description = "Planta tropical de interior.",
            careTips = listOf("Evitar sol directo"),
            facts = listOf("Sus hojas pueden desarrollar fenestraciones")
        )
    )

    private fun plantStatus() = PlantStatusDto(
        statusLabel = "Estres moderado",
        urgency = "medium",
        explanation = "La humedad esta baja.",
        recommendation = "Revisar riego.",
        speciesId = "species-1",
        species = "Monstera deliciosa"
    )

    private fun defaultReadings() = listOf(
        ReadingDto(
            id = "reading-1",
            sensorId = "sensor-1",
            soilMoisture = 55,
            readAt = "2026-07-01T10:00:00",
            source = "sensor"
        ),
        ReadingDto(
            id = "reading-2",
            sensorId = "sensor-1",
            soilMoisture = 20,
            readAt = "2026-06-30T10:00:00",
            source = "sensor"
        )
    )
}
