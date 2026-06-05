package com.example.plant_care_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.plant_care_app.ui.screens.PlantDetailScreen
import com.example.plant_care_app.ui.screens.AddPlantScreen
import com.example.plant_care_app.ui.screens.PlantDetailEvaluationScreen
import com.example.plant_care_app.ui.screens.PlantsOverviewScreen
import com.example.plant_care_app.ui.screens.LoginScreen
import com.example.plant_care_app.ui.screens.RegisterScreen
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import androidx.navigation.NavType
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitClient.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            PlantCareAppTheme {
                App()
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
        composable("add_plant") {
            AddPlantScreen(onBack = { navController.popBackStack() })
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
