package com.example.plant_care_app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.data.SessionManager
import com.example.plant_care_app.ui.models.PlantOverviewDto
import com.example.plant_care_app.ui.models.SensorDto
import com.example.plant_care_app.ui.models.UpdatePlantSensorRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsListScreen(
    onAddSensor: () -> Unit,
    onEditSensor: (String) -> Unit,
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
) {
    val context = LocalContext.current
    var sensors by remember { mutableStateOf<List<SensorDto>>(emptyList()) }
    var plants by remember { mutableStateOf<List<PlantOverviewDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var sensorToDelete by remember { mutableStateOf<SensorDto?>(null) }
    var sensorToAssign by remember { mutableStateOf<SensorDto?>(null) }
    var selectedPlantId by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    suspend fun reload() {
        sensors = RetrofitClient.plantApi.getSensors()
        plants = RetrofitClient.plantApi.getOverview()
    }

    fun load(refresh: Boolean = false) {
        scope.launch {
            if (refresh) isRefreshing = true else isLoading = true
            try {
                reload()
            } catch (e: HttpException) {
                if (e.code() == 401) { SessionManager.clearToken(context); onSessionExpired() }
                else snackbarHostState.showSnackbar("Error al cargar sensores")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error al cargar sensores")
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) { load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis sensores", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSensor,
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar sensor")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { load(refresh = true) },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                }

                sensors.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Sensors,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No tenés sensores registrados",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Presioná + para agregar el primero",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        SensorsHeader(
                            total = sensors.size,
                            available = sensors.count { it.status == "AVAILABLE" }
                        )
                    }
                    items(sensors, key = { it.id }) { sensor ->
                        val assignedPlant = plants.find { it.sensorId == sensor.id }
                        SensorCard(
                            sensor = sensor,
                            assignedPlantName = assignedPlant?.name,
                            onEdit = { onEditSensor(sensor.id) },
                            onDelete = { sensorToDelete = sensor },
                            onAssign = {
                                selectedPlantId = null
                                sensorToAssign = sensor
                            },
                            onUnassign = {
                                val plantId = assignedPlant?.id ?: return@SensorCard
                                isSaving = true
                                scope.launch {
                                    try {
                                        RetrofitClient.plantApi.updatePlantSensor(
                                            plantId, UpdatePlantSensorRequest(sensorId = null)
                                        )
                                        reload()
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("Error al desasignar el sensor")
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo eliminar sensor
    sensorToDelete?.let { sensor ->
        AlertDialog(
            onDismissRequest = { sensorToDelete = null },
            title = { Text("Eliminar sensor") },
            text = { Text("¿Estás seguro que querés eliminar \"${sensor.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    sensorToDelete = null
                    scope.launch {
                        try {
                            RetrofitClient.plantApi.deleteSensor(sensor.id)
                            sensors = sensors.filter { it.id != sensor.id }
                        } catch (e: HttpException) {
                            when (e.code()) {
                                401 -> { SessionManager.clearToken(context); onSessionExpired() }
                                409 -> snackbarHostState.showSnackbar("Este sensor está asignado a una planta")
                                else -> snackbarHostState.showSnackbar("Error al eliminar el sensor")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Error al eliminar el sensor")
                        }
                    }
                }) { Text("Eliminar", color = Color(0xFFD32F2F)) }
            },
            dismissButton = {
                TextButton(onClick = { sensorToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    // Diálogo asignar sensor a planta
    sensorToAssign?.let { sensor ->
        AlertDialog(
            onDismissRequest = { sensorToAssign = null },
            title = { Text("Asignar a una planta") },
            text = {
                if (plants.isEmpty()) {
                    Text("No tenés plantas registradas.", color = Color.Gray)
                } else {
                    Column {
                        plants.forEach { plant ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPlantId = plant.id }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedPlantId == plant.id,
                                    onClick = { selectedPlantId = plant.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2E7D32))
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(plant.name, fontWeight = FontWeight.Medium)
                                    if (plant.hasSensor) {
                                        Text(
                                            "Ya tiene sensor: ${plant.sensorName ?: "asignado"}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = selectedPlantId != null && plants.isNotEmpty() && !isSaving,
                    onClick = {
                        val plantId = selectedPlantId ?: return@TextButton
                        sensorToAssign = null
                        isSaving = true
                        scope.launch {
                            try {
                                RetrofitClient.plantApi.updatePlantSensor(
                                    plantId, UpdatePlantSensorRequest(sensorId = sensor.id)
                                )
                                reload()
                            } catch (e: HttpException) {
                                val msg = if (e.code() == 409) "El sensor ya está asignado a otra planta"
                                          else "Error al asignar el sensor"
                                snackbarHostState.showSnackbar(msg)
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Error al asignar el sensor")
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                ) {
                    if (isSaving) CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF2E7D32)
                    )
                    else Text("Asignar", color = Color(0xFF2E7D32))
                }
            },
            dismissButton = {
                TextButton(onClick = { sensorToAssign = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SensorsHeader(total: Int, available: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            value = total.toString(),
            label = "Total",
            iconTint = Color(0xFF2E7D32),
            bgColor = Color(0xFFE8F5E9)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = available.toString(),
            label = "Disponibles",
            iconTint = Color(0xFF1565C0),
            bgColor = Color(0xFFE3F2FD)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            value = (total - available).toString(),
            label = "Asignados",
            iconTint = Color(0xFF6A1B9A),
            bgColor = Color(0xFFF3E5F5)
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    value: String,
    label: String,
    iconTint: Color,
    bgColor: Color,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = iconTint)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = iconTint.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun SensorCard(
    sensor: SensorDto,
    assignedPlantName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAssign: () -> Unit,
    onUnassign: () -> Unit,
) {
    val isAssigned = sensor.status == "ASSIGNED"
    val borderColor = if (isAssigned) Color(0xFFBDBDBD) else Color(0xFF8BCB8F)
    val iconBg = if (isAssigned) Color(0xFFEEEEEE) else Color(0xFFE8F5E9)
    val iconTint = if (isAssigned) Color(0xFF757575) else Color(0xFF2E7D32)
    val dotColor = if (isAssigned) Color(0xFF757575) else Color(0xFF2E7D32)
    val chipBg = if (isAssigned) Color(0xFFF5F5F5) else Color(0xFFEAF7EA)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono circular
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Sensors,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(Modifier.width(14.dp))

            // Contenido central
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = sensor.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    // Status chip con dot
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(chipBg)
                            .padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = if (isAssigned) "Asignado" else "Disponible",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (isAssigned && assignedPlantName != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocalFlorist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = assignedPlantName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isAssigned) {
                        TextButton(
                            onClick = onUnassign,
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Desasignar", fontSize = 12.sp, color = Color(0xFFD32F2F))
                        }
                    } else {
                        TextButton(
                            onClick = onAssign,
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Asignar a planta", fontSize = 12.sp, color = Color(0xFF2E7D32))
                        }
                    }

                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(2.dp))
                        IconButton(
                            onClick = onDelete,
                            enabled = !isAssigned,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = if (isAssigned) Color.LightGray else Color(0xFFD32F2F),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
