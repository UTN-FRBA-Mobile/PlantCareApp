package com.example.plant_care_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.ui.models.FcmTokenRequest
import com.example.plant_care_app.ui.screens.AddEditSensorScreen
import com.example.plant_care_app.ui.screens.AddPlantScreen
import com.example.plant_care_app.ui.screens.LoginScreen
import com.example.plant_care_app.ui.screens.PlantDetailEvaluationScreen
import com.example.plant_care_app.ui.screens.PlantDetailScreen
import com.example.plant_care_app.ui.screens.PlantsOverviewScreen
import com.example.plant_care_app.ui.screens.RegisterScreen
import com.example.plant_care_app.ui.screens.SensorsListScreen
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            PlantCareAppTheme {
                RequestNotificationPermission()
                App(onLogout = { performLogout() })
            }
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                val token = SessionManager.getFcmToken(applicationContext)
                if (token != null) {
                    RetrofitClient.authApi.logout(FcmTokenRequest(token))
                }
            } catch (e: Exception) {
                // Log error but continue clearing local data
            } finally {
                SessionManager.clearToken(applicationContext)
            }
        }
    }
}

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            // Manejar resultado si es necesario
        }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
private fun App(onLogout: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val startDestination =
        if (SessionManager.getToken(context) != null) "overview" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("overview") {
            PlantsOverviewScreen(
                navController = navController,
                onAddPlant = {
                    navController.navigate("add_plant")
                },
                onLogout = {
                    onLogout()
                    navController.navigate("login") {
                        popUpTo("overview") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "plant_detail/{plantId}",
            deepLinks = listOf(
                navDeepLink { uriPattern = "app://plantcare/plant/{plantId}" }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
            PlantDetailScreen(
                plantId = plantId,
                navController = navController
            )
        }
        composable("add_plant") {
            AddPlantScreen(onBack = { navController.popBackStack() })
        }
        composable("sensors") {
            SensorsListScreen(
                onAddSensor = { navController.navigate("add_sensor") },
                onEditSensor = { id -> navController.navigate("edit_sensor/$id") },
                onBack = { navController.popBackStack() },
                onSessionExpired = {
                    navController.navigate("login") {
                        popUpTo("overview") { inclusive = true }
                    }
                }
            )
        }
        composable("add_sensor") {
            AddEditSensorScreen(sensorId = null, onBack = { navController.popBackStack() })
        }
        composable(
            route = "edit_sensor/{sensorId}",
            arguments = listOf(navArgument("sensorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sensorId = backStackEntry.arguments?.getString("sensorId") ?: ""
            AddEditSensorScreen(sensorId = sensorId, onBack = { navController.popBackStack() })
        }
        composable(
            route = "plant_evaluations/{plantId}/{name}/{type}",
            arguments = listOf(
                navArgument("plantId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: "Planta"
            val type = backStackEntry.arguments?.getString("type") ?: "Especie"
            PlantDetailEvaluationScreen(
                navController = navController,
                plantId = plantId,
                plantName = name,
                plantType = type
            )
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Hola, Planta 🌱")

        Spacer(modifier = Modifier.height(20.dp))

        Image(
            painter = painterResource(id = R.drawable.planta),
            contentDescription = "Imagen de una planta",
            modifier = Modifier.size(220.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PlantCareAppTheme {
//        PlantsOverviewScreen()
    }
}
