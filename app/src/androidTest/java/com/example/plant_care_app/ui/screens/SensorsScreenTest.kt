package com.example.plant_care_app.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.plant_care_app.data.PlantApiService
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.models.CreateSensorRequest
import com.example.plant_care_app.ui.models.PlantOverviewDto
import com.example.plant_care_app.ui.models.SensorDto
import com.example.plant_care_app.ui.models.UpdateSensorRequest
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SensorsScreenTest {

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
    fun sensorsListDisplaysLoadedSensorsAndAssignedPlant() {
        givenSensors(
            sensors = listOf(
                sensor(id = "sensor-1", name = "Sensor patio", status = "AVAILABLE"),
                sensor(id = "sensor-2", name = "Sensor cocina", status = "ASSIGNED")
            ),
            plants = listOf(
                plant(id = "plant-1", name = "Monstera", sensorId = "sensor-2")
            )
        )

        setSensorsListScreen()
        waitForText("Sensor patio")

        composeTestRule.onNodeWithText("Mis sensores").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sensor patio").assertIsDisplayed()
        composeTestRule.onNodeWithText("Disponible").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sensor cocina").assertIsDisplayed()
        composeTestRule.onNodeWithText("Asignado").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monstera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Asignar a planta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Desasignar").assertIsDisplayed()
    }

    @Test
    fun sensorsListDisplaysEmptyStateWhenNoSensorsExist() {
        givenSensors(sensors = emptyList(), plants = emptyList())

        setSensorsListScreen()
        waitForText("No tenés sensores registrados")

        composeTestRule.onNodeWithText("No tenés sensores registrados").assertIsDisplayed()
        composeTestRule.onNodeWithText("Presioná + para agregar el primero").assertIsDisplayed()
    }

    @Test
    fun sensorsListCallsNavigationCallbacks() {
        var addClicks = 0
        var backClicks = 0
        var editedSensorId: String? = null
        givenSensors(sensors = listOf(sensor(id = "sensor-1", name = "Sensor patio")), plants = emptyList())

        setSensorsListScreen(
            onAddSensor = { addClicks += 1 },
            onBack = { backClicks += 1 },
            onEditSensor = { editedSensorId = it }
        )
        waitForText("Sensor patio")

        composeTestRule.onNodeWithContentDescription("Agregar sensor").performClick()
        composeTestRule.onNodeWithContentDescription("Volver").performClick()
        composeTestRule.onNodeWithContentDescription("Editar").performClick()

        assertEquals(1, addClicks)
        assertEquals(1, backClicks)
        assertEquals("sensor-1", editedSensorId)
    }

    @Test
    fun sensorsListDeletesAvailableSensorAfterConfirmation() {
        val sensor = sensor(id = "sensor-1", name = "Sensor patio", status = "AVAILABLE")
        givenSensors(sensors = listOf(sensor), plants = emptyList())
        coEvery { plantApi.deleteSensor("sensor-1") } returns sensor

        setSensorsListScreen()
        waitForText("Sensor patio")

        composeTestRule.onNodeWithContentDescription("Eliminar").performClick()
        composeTestRule.onNodeWithText("¿Estás seguro que querés eliminar \"Sensor patio\"?").assertIsDisplayed()
        composeTestRule
            .onNode(hasText("Eliminar") and hasClickAction())
            .performClick()

        coVerify(exactly = 1) { plantApi.deleteSensor("sensor-1") }
    }

    @Test
    fun addSensorShowsValidationErrorsForEmptyFields() {
        setAddEditSensorScreen(sensorId = null)

        composeTestRule
            .onNode(hasText("Vincular sensor") and hasClickAction())
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithText("El nombre es obligatorio.").assertIsDisplayed()

        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Sensor patio")
        composeTestRule
            .onNode(hasText("Vincular sensor") and hasClickAction())
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithText("El codigo del sensor es obligatorio.").assertIsDisplayed()
    }

    @Test
    fun addSensorCreatesSensorAndCallsCallbacks() {
        var createdSensorId: String? = null
        var backClicks = 0
        coEvery {
            plantApi.createSensor(CreateSensorRequest(name = "Sensor patio", apiKey = "API-123"))
        } returns sensor(id = "sensor-created", name = "Sensor patio", apiKey = "API-123")

        setAddEditSensorScreen(
            sensorId = null,
            onBack = { backClicks += 1 },
            onSensorCreated = { createdSensorId = it }
        )

        composeTestRule.onAllNodes(hasSetTextAction())[0].performTextInput("Sensor patio")
        composeTestRule.onAllNodes(hasSetTextAction())[1].performTextInput("API-123")
        composeTestRule
            .onNode(hasText("Vincular sensor") and hasClickAction())
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) { createdSensorId == "sensor-created" }
        assertEquals(1, backClicks)
        coVerify(exactly = 1) {
            plantApi.createSensor(CreateSensorRequest(name = "Sensor patio", apiKey = "API-123"))
        }
    }

    @Test
    fun editSensorLoadsSensorAndSavesChanges() {
        var backClicks = 0
        coEvery { plantApi.getSensorById("sensor-1") } returns sensor(
            id = "sensor-1",
            name = "Sensor patio",
            apiKey = "API-123"
        )
        coEvery {
            plantApi.updateSensor("sensor-1", UpdateSensorRequest(name = "Sensor patio"))
        } returns sensor(id = "sensor-1", name = "Sensor patio", apiKey = "API-123")

        setAddEditSensorScreen(
            sensorId = "sensor-1",
            onBack = { backClicks += 1 }
        )
        waitForText("Sensor patio")

        composeTestRule.onNodeWithText("API-123").assertIsDisplayed()
        composeTestRule
            .onNode(hasText("Guardar cambios") and hasClickAction())
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) { backClicks == 1 }
        coVerify(exactly = 1) {
            plantApi.updateSensor("sensor-1", UpdateSensorRequest(name = "Sensor patio"))
        }
    }

    private fun setSensorsListScreen(
        onAddSensor: () -> Unit = {},
        onEditSensor: (String) -> Unit = {},
        onBack: () -> Unit = {},
        onSessionExpired: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            PlantCareAppTheme {
                SensorsListScreen(
                    onAddSensor = onAddSensor,
                    onEditSensor = onEditSensor,
                    onBack = onBack,
                    onSessionExpired = onSessionExpired
                )
            }
        }
    }

    private fun setAddEditSensorScreen(
        sensorId: String?,
        onBack: () -> Unit = {},
        onSensorCreated: (String) -> Unit = {}
    ) {
        composeTestRule.setContent {
            PlantCareAppTheme {
                AddEditSensorScreen(
                    sensorId = sensorId,
                    onBack = onBack,
                    onSensorCreated = onSensorCreated
                )
            }
        }
    }

    private fun givenSensors(
        sensors: List<SensorDto>,
        plants: List<PlantOverviewDto>
    ) {
        coEvery { plantApi.getSensors() } returns sensors
        coEvery { plantApi.getOverview() } returns plants
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasText(text))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun sensor(
        id: String,
        name: String,
        status: String = "AVAILABLE",
        apiKey: String? = null
    ) = SensorDto(
        id = id,
        name = name,
        status = status,
        apiKey = apiKey,
        userId = "user-1",
        createdAt = "2026-07-01T12:00:00Z"
    )

    private fun plant(
        id: String,
        name: String,
        sensorId: String?
    ) = PlantOverviewDto(
        id = id,
        name = name,
        location = "Living",
        imageUrl = null,
        sensorId = sensorId,
        sensorName = null,
        hasSensor = sensorId != null,
        soilMoisture = 55,
        readAt = null,
        recommendation = null,
        urgency = null,
        statusLabel = "Saludable"
    )
}
