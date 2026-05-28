package com.example.plant_care_app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plant_care_app.R
import com.example.plant_care_app.data.PlantImageStore
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.ui.components.PlantCard
import com.example.plant_care_app.ui.models.PlantOverviewDto
import com.example.plant_care_app.ui.theme.PlantCareAppTheme

data class PlantUi(
    val name: String,
    val location: String,
    val humidity: Int,
    val status: String
)

@Composable
fun PlantsOverviewScreen(
    onAddPlant: () -> Unit = {},
    onLogout: () -> Unit = {},
    navController: NavController
) {

    var plants by remember { mutableStateOf<List<PlantOverviewDto>>(emptyList()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            plants = RetrofitClient.plantApi.getOverview()
        } catch (e: retrofit2.HttpException) {
            if (e.code() == 401) {
                SessionManager.clearToken(context)

                navController.navigate("login") {
                    popUpTo("overview") { inclusive = true }
                }
            } else {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    PlantsOverviewContent(
        plants = plants,
        onPlantClick = { plantId -> navController.navigate("plant_detail/$plantId") },
        onAddClick = onAddPlant,
        onLogoutClick = onLogout
    )
}

@Composable
private fun PlantsOverviewContent(
    plants: List<PlantOverviewDto>,
    onPlantClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_image),
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
            )

            Column {
                Text(
                    text = "Mis Plantas",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Cerrar sesión",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        onLogoutClick()
                    }
                )
            }

            Button(
                onClick = onAddClick,

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(plants) { plant ->
                val localImagePath = PlantImageStore.getImagePath(context, plant.id)
                PlantCard(
                    name = plant.name,
                    location = plant.location,
                    humidity = plant.soilMoisture ?: 0,
                    status = plant.statusLabel ?: "Sin lecturas",
                    imageUrl = localImagePath,
                    onClick = { onPlantClick(plant.id) },
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlantsOverviewContentPreview() {
    PlantCareAppTheme {
        PlantsOverviewContent(
            plants = listOf(
                PlantOverviewDto(
                    id = "1", name = "Albahaca", location = "Balcón", null,
                    sensorId = "s1", sensorName = "Sensor 1", hasSensor = true,
                    soilMoisture = 65, readAt = "2026-05-09T10:00:00.000Z",
                    recommendation = null, urgency = null, statusLabel = "Saludable"
                ),
                PlantOverviewDto(
                    id = "2", name = "Lavanda", location = "Ventana", null,
                    sensorId = null, sensorName = null, hasSensor = false,
                    soilMoisture = 28, readAt = "2026-05-08T10:00:00.000Z",
                    recommendation = null, urgency = null, statusLabel = "Estres alto"
                ),
                PlantOverviewDto(
                    id = "3", name = "Romero", location = "Jardín", null,
                    sensorId = "s2", sensorName = "Sensor 2", hasSensor = true,
                    soilMoisture = 45, readAt = "2026-05-07T10:00:00.000Z",
                    recommendation = null, urgency = null, statusLabel = "Estres moderado"
                ),
            ),
            onPlantClick = {},
            onAddClick = {},
            onLogoutClick = {}
        )
    }
}
