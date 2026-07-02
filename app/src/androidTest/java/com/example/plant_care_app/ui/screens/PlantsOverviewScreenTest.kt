package com.example.plant_care_app.ui.screens

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.plant_care_app.data.PlantApiService
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.models.PlantOverviewDto
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PlantsOverviewScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var plantApi: PlantApiService

    @Before
    fun setUp() {
        plantApi = mockk()
        mockkObject(RetrofitClient)
        every { RetrofitClient.plantApi } returns plantApi
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitClient)
    }

    @Test
    fun overviewDisplaysLoadedPlantsAndAttentionSummary() {
        givenOverviewPlants(defaultPlants())

        setOverviewScreen()
        waitForText("Monstera")

        composeTestRule.onNodeWithText("Plant Care App").assertIsDisplayed()
        composeTestRule.onNodeWithText("Requieren atencion").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 planta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tocar para revisar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Todas las plantas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monstera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Living").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pothos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cocina").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sensor cocina").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sin sensor").assertIsDisplayed()
    }

    @Test
    fun overviewAddButtonCallsCallback() {
        var addClicks = 0
        givenOverviewPlants(emptyList())

        setOverviewScreen(onAddPlant = { addClicks += 1 })

        composeTestRule
            .onNodeWithContentDescription("Agregar planta")
            .performClick()

        org.junit.Assert.assertEquals(1, addClicks)
    }

    @Test
    fun overviewLogoutButtonCallsCallback() {
        var logoutClicks = 0
        givenOverviewPlants(emptyList())

        setOverviewScreen(onLogout = { logoutClicks += 1 })

        composeTestRule
            .onNodeWithContentDescription("Cerrar sesion")
            .performClick()

        org.junit.Assert.assertEquals(1, logoutClicks)
    }

    @Test
    fun overviewSensorsButtonNavigatesToSensorsScreen() {
        givenOverviewPlants(emptyList())

        setOverviewNavHost()

        composeTestRule
            .onNodeWithContentDescription("Gestionar sensores")
            .performClick()

        composeTestRule
            .onNodeWithText("Sensors destination")
            .assertIsDisplayed()
    }

    @Test
    fun overviewPlantClickNavigatesToPlantDetailScreen() {
        givenOverviewPlants(defaultPlants())

        setOverviewNavHost()
        waitForText("Monstera")

        composeTestRule
            .onNode(hasText("Monstera") and hasClickAction())
            .performClick()

        composeTestRule
            .onNodeWithText("Detail plant-monstera")
            .assertIsDisplayed()
    }

    private fun givenOverviewPlants(plants: List<PlantOverviewDto>) {
        coEvery { plantApi.getOverview() } returns plants
    }

    private fun setOverviewScreen(
        onAddPlant: () -> Unit = {},
        onLogout: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            val navController = rememberNavController()

            PlantCareAppTheme {
                PlantsOverviewScreen(
                    navController = navController,
                    onAddPlant = onAddPlant,
                    onLogout = onLogout
                )
            }
        }
    }

    private fun setOverviewNavHost() {
        composeTestRule.setContent {
            val navController = rememberNavController()

            PlantCareAppTheme {
                NavHost(navController = navController, startDestination = "overview") {
                    composable("overview") {
                        PlantsOverviewScreen(navController = navController)
                    }
                    composable("sensors") {
                        Text("Sensors destination")
                    }
                    composable("plant_detail/{plantId}") { backStackEntry ->
                        Text("Detail ${backStackEntry.arguments?.getString("plantId")}")
                    }
                    composable("login") {
                        Text("Login destination")
                    }
                }
            }
        }
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasText(text))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun defaultPlants() = listOf(
        plantOverview(
            id = "plant-monstera",
            name = "Monstera",
            location = "Living",
            sensorId = "sensor-1",
            sensorName = "Sensor cocina",
            hasSensor = true,
            soilMoisture = 62,
            statusLabel = "Saludable",
            urgency = null
        ),
        plantOverview(
            id = "plant-pothos",
            name = "Pothos",
            location = "Cocina",
            sensorId = null,
            sensorName = null,
            hasSensor = false,
            soilMoisture = 24,
            statusLabel = "Estres moderado",
            urgency = "medium"
        )
    )

    private fun plantOverview(
        id: String,
        name: String,
        location: String,
        sensorId: String?,
        sensorName: String?,
        hasSensor: Boolean,
        soilMoisture: Int?,
        statusLabel: String?,
        urgency: String?
    ) = PlantOverviewDto(
        id = id,
        name = name,
        location = location,
        imageUrl = null,
        sensorId = sensorId,
        sensorName = sensorName,
        hasSensor = hasSensor,
        soilMoisture = soilMoisture,
        readAt = null,
        recommendation = null,
        urgency = urgency,
        statusLabel = statusLabel
    )
}
