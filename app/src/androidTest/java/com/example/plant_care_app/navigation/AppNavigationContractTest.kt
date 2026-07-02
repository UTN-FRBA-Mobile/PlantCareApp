package com.example.plant_care_app.navigation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.core.app.ApplicationProvider
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import org.junit.After
import org.junit.Rule
import org.junit.Test

class AppNavigationContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @After
    fun tearDown() {
        SessionManager.clearToken(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun appStartsAtLoginWhenThereIsNoSavedToken() {
        SessionManager.clearToken(ApplicationProvider.getApplicationContext())

        setTestAppNavHost()

        composeTestRule.onNodeWithText("Login destination").assertIsDisplayed()
    }

    @Test
    fun appStartsAtOverviewWhenThereIsSavedToken() {
        SessionManager.saveToken(ApplicationProvider.getApplicationContext(), "token-123")

        setTestAppNavHost()

        composeTestRule.onNodeWithText("Overview destination").assertIsDisplayed()
    }

    @Test
    fun logoutFromOverviewClearsTokenAndNavigatesToLogin() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        SessionManager.saveToken(context, "token-123")

        setTestAppNavHost()
        composeTestRule.onNodeWithText("Logout").performClick()

        composeTestRule.onNodeWithText("Login destination").assertIsDisplayed()
        org.junit.Assert.assertNull(SessionManager.getToken(context))
    }

    @Test
    fun overviewCanNavigateToAddPlantAndCreatedPlantDetail() {
        setTestAppNavHost(startDestinationOverride = "overview")

        composeTestRule.onNodeWithText("Add plant").performClick()
        composeTestRule.onNodeWithText("Add plant destination").assertIsDisplayed()

        composeTestRule.onNodeWithText("Create plant").performClick()
        composeTestRule.onNodeWithText("Plant detail plant-created").assertIsDisplayed()
    }

    @Test
    fun overviewPlantClickNavigatesToDetailAndDetailNavigatesToEvaluations() {
        setTestAppNavHost(startDestinationOverride = "overview")

        composeTestRule.onNodeWithText("Open plant").performClick()
        composeTestRule.onNodeWithText("Plant detail plant-1").assertIsDisplayed()

        composeTestRule.onNodeWithText("Open evaluations").performClick()
        composeTestRule
            .onNodeWithText("Evaluations plant-1 Monstera Monstera deliciosa")
            .assertIsDisplayed()
    }

    @Test
    fun addPlantCanCreateSensorAndReceiveLinkedSensorId() {
        setTestAppNavHost(startDestinationOverride = "add_plant")

        composeTestRule.onNodeWithText("Linked sensor: none").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add sensor").performClick()
        composeTestRule.onNodeWithText("Add sensor destination").assertIsDisplayed()

        composeTestRule.onNodeWithText("Create sensor").performClick()
        composeTestRule.onNodeWithText("Linked sensor: sensor-created").assertIsDisplayed()
    }

    @Test
    fun sensorsCanNavigateToAddSensorRoute() {
        setTestAppNavHost(startDestinationOverride = "sensors")

        composeTestRule.onNodeWithText("Add sensor").performClick()
        composeTestRule.onNodeWithText("Add sensor destination").assertIsDisplayed()
    }

    @Test
    fun sensorsCanNavigateToEditSensorRoute() {
        setTestAppNavHost(startDestinationOverride = "sensors")

        composeTestRule.onNodeWithText("Edit sensor").performClick()
        composeTestRule.onNodeWithText("Edit sensor sensor-1").assertIsDisplayed()
    }

    private fun setTestAppNavHost(startDestinationOverride: String? = null) {
        composeTestRule.setContent {
            PlantCareAppTheme {
                TestAppNavHost(startDestinationOverride = startDestinationOverride)
            }
        }
    }
}

@Composable
private fun TestAppNavHost(startDestinationOverride: String? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val startDestination = startDestinationOverride
        ?: if (SessionManager.getToken(context) != null) "overview" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            Text("Login destination")
        }
        composable("register") {
            Text("Register destination")
        }
        composable("overview") {
            Column {
                Text("Overview destination")
                Button(onClick = { navController.navigate("add_plant") }) {
                    Text("Add plant")
                }
                Button(onClick = { navController.navigate("plant_detail/plant-1") }) {
                    Text("Open plant")
                }
                Button(onClick = { navController.navigate("sensors") }) {
                    Text("Open sensors")
                }
                Button(
                    onClick = {
                        SessionManager.clearToken(context)
                        navController.navigate("login") {
                            popUpTo("overview") { inclusive = true }
                        }
                    }
                ) {
                    Text("Logout")
                }
            }
        }
        composable("plant_detail/{plantId}") { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
            Column {
                Text("Plant detail $plantId")
                Button(
                    onClick = {
                        navController.navigate(
                            "plant_evaluations/$plantId/Monstera/Monstera deliciosa"
                        )
                    }
                ) {
                    Text("Open evaluations")
                }
            }
        }
        composable("add_plant") { backStackEntry ->
            val linkedSensorId by backStackEntry.savedStateHandle
                .getStateFlow<String?>("linkedSensorId", null)
                .collectAsState()

            Column {
                Text("Add plant destination")
                Text("Linked sensor: ${linkedSensorId ?: "none"}")
                Button(onClick = { navController.navigate("add_sensor") }) {
                    Text("Add sensor")
                }
                Button(
                    onClick = {
                        navController.navigate("plant_detail/plant-created") {
                            popUpTo("add_plant") { inclusive = true }
                        }
                    }
                ) {
                    Text("Create plant")
                }
            }
        }
        composable("sensors") {
            Column {
                Text("Sensors destination")
                Button(onClick = { navController.navigate("add_sensor") }) {
                    Text("Add sensor")
                }
                Button(onClick = { navController.navigate("edit_sensor/sensor-1") }) {
                    Text("Edit sensor")
                }
            }
        }
        composable("add_sensor") {
            Column {
                Text("Add sensor destination")
                Button(
                    onClick = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("linkedSensorId", "sensor-created")
                        navController.popBackStack()
                    }
                ) {
                    Text("Create sensor")
                }
            }
        }
        composable(
            route = "edit_sensor/{sensorId}",
            arguments = listOf(navArgument("sensorId") { type = NavType.StringType })
        ) { backStackEntry ->
            Text("Edit sensor ${backStackEntry.arguments?.getString("sensorId")}")
        }
        composable(
            route = "plant_evaluations/{plantId}/{name}/{type}",
            arguments = listOf(
                navArgument("plantId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            Text(
                "Evaluations " +
                    "${backStackEntry.arguments?.getString("plantId")} " +
                    "${backStackEntry.arguments?.getString("name")} " +
                    "${backStackEntry.arguments?.getString("type")}"
            )
        }
    }
}
