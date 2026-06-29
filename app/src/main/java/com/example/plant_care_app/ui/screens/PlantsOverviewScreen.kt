package com.example.plant_care_app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.plant_care_app.R
import com.example.plant_care_app.data.NotificationPreferenceStore
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.notifications.PlantAlertNotificationManager
import com.example.plant_care_app.notifications.PlantReminderService
import com.example.plant_care_app.ui.components.PlantCard
import com.example.plant_care_app.ui.models.PlantOverviewDto
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsOverviewScreen(
    onAddPlant: () -> Unit = {},
    onLogout: () -> Unit = {},
    navController: NavController
) {
    var plants by remember { mutableStateOf<List<PlantOverviewDto>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var pendingAlertPlants by remember { mutableStateOf<List<PlantOverviewDto>>(emptyList()) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notificationStore = remember { NotificationPreferenceStore(context.applicationContext) }
    val plantAlertNotificationManager = remember {
        PlantAlertNotificationManager(
            notificationStore = notificationStore,
            plantReminderService = PlantReminderService(context.applicationContext)
        )
    }

    // Launcher de Compose para pedir el permiso de notificaciones en Android 13+.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            plantAlertNotificationManager.notifyHighStressPlants(pendingAlertPlants)
        } else {
            Toast.makeText(context, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show()
        }

        pendingAlertPlants = emptyList()
    }

    // Revisa el overview cargado y dispara alertas locales solo para plantas en estres alto.
    fun processPlantAlertNotifications(overviewPlants: List<PlantOverviewDto>) {
        val alertPlants = plantAlertNotificationManager.getPendingHighStressAlerts(overviewPlants)

        if (alertPlants.isEmpty()) {
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            plantAlertNotificationManager.notifyHighStressPlants(alertPlants)
        } else if (!notificationStore.hasAskedNotificationPermission()) {
            pendingAlertPlants = alertPlants
            notificationStore.markNotificationPermissionAsked()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            pendingAlertPlants = emptyList()
        }
    }

    suspend fun refreshPlants() {
        isRefreshing = true

        when (val result = loadPlantsOverview()) {
            is PlantsOverviewLoadResult.Success -> {
                plants = result.plants
                processPlantAlertNotifications(result.plants)
            }

            PlantsOverviewLoadResult.Unauthorized -> {
                SessionManager.clearToken(context)

                navController.navigate("login") {
                    popUpTo("overview") { inclusive = true }
                }
            }

            PlantsOverviewLoadResult.Error -> Unit
        }

        isRefreshing = false
    }

    LaunchedEffect(Unit) {
        refreshPlants()
    }

    PlantsOverviewContent(
        plants = plants,
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                refreshPlants()
            }
        },
        onPlantClick = { plantId -> navController.navigate("plant_detail/$plantId") },
        onAddClick = onAddPlant,
        onLogoutClick = onLogout,
        onSensorsClick = { navController.navigate("sensors") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantsOverviewContent(
    plants: List<PlantOverviewDto>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onPlantClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSensorsClick: () -> Unit = {}
) {
    val plantsRequiringAttention = plants.filter { it.requiresAttention() }
    val connectedSensors = plants.count { it.hasSensor }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar planta"
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OverviewHeader(
                        onLogoutClick = onLogoutClick,
                        onSensorsClick = onSensorsClick
                    )
                }

                item {
                    DashboardSummary(
                        totalPlants = plants.size,
                        connectedSensors = connectedSensors,
                        alerts = plantsRequiringAttention.size
                    )
                }

                if (plantsRequiringAttention.isNotEmpty()) {
                    item {
                        SectionTitle(text = "Requieren atencion")
                    }

                    if (plantsRequiringAttention.size == 1) {
                        item(key = "attention-summary-${plantsRequiringAttention.first().id}") {
                            AttentionSummaryCard(
                                count = plantsRequiringAttention.size,
                                onClick = { onPlantClick(plantsRequiringAttention.first().id) }
                            )
                        }
                    } else {
                        items(
                            items = plantsRequiringAttention,
                            key = { "attention-${it.id}" }
                        ) { plant ->
                            OverviewPlantCard(
                                plant = plant,
                                onPlantClick = onPlantClick
                            )
                        }
                    }
                }

                item {
                    SectionTitle(text = "Todas las plantas")
                }

                items(
                    items = plants,
                    key = { "all-${it.id}" }
                ) { plant ->
                    OverviewPlantCard(
                        plant = plant,
                        onPlantClick = onPlantClick
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewHeader(
    onLogoutClick: () -> Unit,
    onSensorsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_image),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.size(12.dp))

            Column {
                Text(
                    text = "Plant Care App",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Tus plantas están siendo monitoreadas",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onSensorsClick) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = "Gestionar sensores",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        IconButton(onClick = onLogoutClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = "Cerrar sesion",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AttentionSummaryCard(
    count: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, Color(0xFFE9B65F)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFAEF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFE69A18),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.size(12.dp))

                Column {
                    Text(
                        text = "$count planta",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tocar para revisar",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardSummary(
    totalPlants: Int,
    connectedSensors: Int,
    alerts: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SummaryCard(
            value = totalPlants.toString(),
            label = "Plantas",
            icon = Icons.Default.LocalFlorist,
            iconTint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            value = connectedSensors.toString(),
            label = "Sensores",
            icon = Icons.Default.Sensors,
            iconTint = Color(0xFF2589BD),
            modifier = Modifier.weight(1f)
        )

        SummaryCard(
            value = alerts.toString(),
            label = "Alertas",
            icon = Icons.Default.Warning,
            iconTint = if (alerts > 0) Color(0xFFE69A18) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    value: String,
    label: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(92.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun OverviewPlantCard(
    plant: PlantOverviewDto,
    onPlantClick: (String) -> Unit
) {
    PlantCard(
        plantId = plant.id,
        name = plant.name,
        location = plant.location,
        humidity = plant.soilMoisture ?: 0,
        status = plant.statusLabel ?: "Sin lecturas",
        imageUrl = plant.imageUrl,
        sensorName = plant.sensorName,
        hasSensor = plant.hasSensor,
        onClick = { onPlantClick(plant.id) },
    )
}

private fun PlantOverviewDto.requiresAttention(): Boolean {
    val status = statusLabel.orEmpty().lowercase()
    val urgencyValue = urgency.orEmpty().lowercase()

    return "alto" in status ||
        "moderado" in status ||
        urgencyValue in setOf("medium", "high", "moderate", "moderado", "alta", "alto")
}

private sealed interface PlantsOverviewLoadResult {
    data class Success(val plants: List<PlantOverviewDto>) : PlantsOverviewLoadResult
    data object Unauthorized : PlantsOverviewLoadResult
    data object Error : PlantsOverviewLoadResult
}

private suspend fun loadPlantsOverview(): PlantsOverviewLoadResult {
    return try {
        PlantsOverviewLoadResult.Success(RetrofitClient.plantApi.getOverview())
    } catch (e: HttpException) {
        if (e.code() == 401) {
            PlantsOverviewLoadResult.Unauthorized
        } else {
            e.printStackTrace()
            PlantsOverviewLoadResult.Error
        }
    } catch (e: Exception) {
        e.printStackTrace()
        PlantsOverviewLoadResult.Error
    }
}

@Preview(showBackground = true)
@Composable
private fun PlantsOverviewContentPreview() {
    PlantCareAppTheme {
        PlantsOverviewContent(
            plants = listOf(
                PlantOverviewDto(
                    id = "1", name = "Albahaca", location = "Balcon", null,
                    sensorId = "s1", sensorName = "Sensor 1", hasSensor = true,
                    soilMoisture = 65, readAt = "2026-05-09T10:00:00.000Z",
                    recommendation = null, urgency = null, statusLabel = "Saludable"
                ),
                PlantOverviewDto(
                    id = "2", name = "Lavanda", location = "Ventana", null,
                    sensorId = null, sensorName = null, hasSensor = false,
                    soilMoisture = 28, readAt = "2026-05-08T10:00:00.000Z",
                    recommendation = null, urgency = "high", statusLabel = "Estres alto"
                ),
                PlantOverviewDto(
                    id = "3", name = "Romero", location = "Jardin", null,
                    sensorId = "s2", sensorName = "Sensor 2", hasSensor = true,
                    soilMoisture = 45, readAt = "2026-05-07T10:00:00.000Z",
                    recommendation = null, urgency = "medium", statusLabel = "Estres moderado"
                ),
            ),
            onPlantClick = {},
            isRefreshing = false,
            onRefresh = {},
            onAddClick = {},
            onLogoutClick = {}
        )
    }
}
