package com.example.plant_care_app

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.plant_care_app.ui.models.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.ui.screens.AddEditSensorScreen
import com.example.plant_care_app.ui.screens.PlantDetailScreen
import com.example.plant_care_app.ui.screens.AddPlantScreen
import com.example.plant_care_app.ui.screens.PlantDetailEvaluationScreen
import com.example.plant_care_app.ui.screens.PlantsOverviewScreen
import com.example.plant_care_app.ui.screens.LoginScreen
import com.example.plant_care_app.ui.screens.RegisterScreen
import com.example.plant_care_app.ui.screens.SensorsListScreen
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        
        // Obtención del FCM Token para notificaciones push
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Error al obtener el token de FCM", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("MainActivity", "FCM Token: $token")
            sendTokenToBackend(token)
        }

        enableEdgeToEdge()
        setContent {
            PlantCareAppTheme {
                App()
            }
        }
    }

    private fun sendTokenToBackend(token: String) {
        val request = FcmTokenRequest(
            token = token,
            deviceModel = Build.MODEL
        )

        lifecycleScope.launch {
            try {
                // El endpoint requiere autenticación. Solo enviamos si hay sesión activa.
                if (SessionManager.getToken(applicationContext) != null) {
                    RetrofitClient.authApi.registerFcmToken(request)
                    Log.i("MainActivity", "FCM Token registrado con éxito en el backend")
                } else {
                    Log.i("MainActivity", "FCM Token obtenido, pero esperando a login para registrarlo")
                }
            } catch (e: Exception) {
                // Si falla (ej. 401), se volverá a intentar en el próximo inicio o cambio de token
                Log.e("MainActivity", "Error al registrar FCM Token en backend: ${e.message}")
            }
        }
    }
}

@Composable
private fun App(){
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current

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
                    SessionManager.clearToken(context)

                    navController.navigate("login") {
                        popUpTo("overview") { inclusive = true }
                    }
                }
            )
        }
        composable("plant_detail/{plantId}") { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
            PlantDetailScreen(
                plantId = plantId,
                navController = navController
            )
        }
        composable("add_plant") { backStackEntry ->
            val linkedSensorId by backStackEntry.savedStateHandle
                .getStateFlow<String?>("linkedSensorId", null)
                .collectAsState()

            AddPlantScreen(
                onBack = { navController.popBackStack() },
                onAddSensor = { navController.navigate("add_sensor") },
                // Al crear una planta se redirige naturalmente al Detalle de la Planta
                onPlantCreated = { plantId ->
                    navController.navigate("plant_detail/$plantId") {
                        popUpTo("add_plant") { inclusive = true }
                    }
                },
                linkedSensorId = linkedSensorId,
                onLinkedSensorHandled = {
                    backStackEntry.savedStateHandle.remove<String>("linkedSensorId")
                }
            )
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
            AddEditSensorScreen(
                sensorId = null,
                onBack = { navController.popBackStack() },
                onSensorCreated = { sensorId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("linkedSensorId", sensorId)
                }
            )
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
