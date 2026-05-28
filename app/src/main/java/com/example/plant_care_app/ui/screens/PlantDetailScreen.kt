package com.example.plant_care_app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.plant_care_app.R
import com.example.plant_care_app.data.PlantImageStore
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.models.PlantDetailDto
import com.example.plant_care_app.ui.models.PlantStatusDto
import com.example.plant_care_app.ui.models.ReadingDto
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import com.example.plant_care_app.utils.PlantImageFileManager
import com.example.plant_care_app.utils.PlantImageResolver
import java.io.File

@Composable
fun PlantDetailScreen(plantId: String, navController: NavController) {

    var plant by remember { mutableStateOf<PlantDetailDto?>(null) }
    var readings by remember { mutableStateOf<List<ReadingDto>>(emptyList()) }
    var status by remember { mutableStateOf<PlantStatusDto?>(null) }

    LaunchedEffect(plantId) {
        plant = RetrofitClient.plantApi.getPlantById(plantId)
        readings = RetrofitClient.plantApi.getReadings(plantId)
            .sortedByDescending { it.readAt }
        status = RetrofitClient.plantApi.getPlantStatus(plantId)
    }

    PlantDetailContent(
        plant = plant,
        readings = readings,
        status = status,
        onBack = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantDetailContent(
    plant: PlantDetailDto?,
    readings: List<ReadingDto>,
    status: PlantStatusDto?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var photoFile by remember { mutableStateOf<File?>(null) }

    // Estado local para refrescar la imagen apenas el usuario toma o elige una foto.
    var localImagePath by remember(plant?.id) {
        mutableStateOf(
            plant?.id?.let { PlantImageStore.getImagePath(context, it) }
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && plant?.id != null && photoFile != null) {
            PlantImageStore.saveImagePath(
                context = context,
                plantId = plant.id,
                imagePath = photoFile!!.absolutePath
            )

            localImagePath = photoFile!!.absolutePath
        }
    }

    fun launchCamera() {
        val currentPlantId = plant?.id ?: return

        val file = PlantImageFileManager.createImageFile(
            context = context,
            plantId = currentPlantId
        )

        val uri = PlantImageFileManager.getUriForFile(context, file)

        photoFile = file
        cameraLauncher.launch(uri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->

        val currentPlantId = plant?.id ?: return@rememberLauncherForActivityResult

        if (uri != null) {

            val file = PlantImageFileManager.copyUriToPlantImageFile(
                context = context,
                uri = uri,
                plantId = currentPlantId
            )

            PlantImageStore.saveImagePath(
                context = context,
                plantId = currentPlantId,
                imagePath = file.absolutePath
            )

            localImagePath = file.absolutePath
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Plant Care App",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    Image(
                        painter = painterResource(id = R.drawable.app_image),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                val imageModel = plant?.let {
                    PlantImageResolver.resolve(
                        context = context,
                        plantId = it.id,
                        imageUrl = it.imageUrl,
                        localImagePath = localImagePath
                    )
                }

                AsyncImage(
                    model = imageModel ?: R.drawable.planta,
                    contentDescription = plant?.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.planta),
                    error = painterResource(R.drawable.planta)
                )
            }

            item {

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Button(
                        onClick = { launchCamera() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32)
                        )
                    ) {
                        Text(
                            text = if (localImagePath == null)
                                "Tomar foto"
                            else
                                "Actualizar con cámara",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF558B2F)
                        )
                    ) {
                        Text(
                            text = if (localImagePath == null)
                                "Elegir desde galería"
                            else
                                "Actualizar desde galería",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Column {
                    Text(
                        text = plant?.name ?: "",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = plant?.species ?: "",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍  ${plant?.location ?: ""}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            item {
                StatusHumidityCard(
                    statusLabel = status?.statusLabel,
                    explanation = status?.explanation,
                    soilMoisture = readings.firstOrNull()?.soilMoisture
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NavCard(
                        emoji = "📷",
                        label = "Galería",
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ }
                    )
                    NavCard(
                        emoji = "📊",
                        label = "Evaluaciones",
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ }
                    )
                }
            }

            item {
                Text(
                    text = "Historial de mediciones",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (readings.isEmpty()) {
                item {
                    Text(text = "Sin mediciones registradas", color = Color.Gray)
                }
            }

            items(readings) { reading ->
                ReadingItem(reading)
            }
        }
    }
}

@Composable
private fun StatusHumidityCard(
    statusLabel: String?,
    explanation: String?,
    soilMoisture: Int?
) {
    val statusColor = when (statusLabel) {
        "Saludable" -> Color(0xFF2E7D32)
        "Estres moderado" -> Color(0xFFE65100)
        "Estres alto" -> Color(0xFFB71C1C)
        else -> Color(0xFF757575)
    }
    val bgColor = when (statusLabel) {
        "Saludable" -> Color(0xFFC8E6C9)
        "Estres moderado" -> Color(0xFFFFE0B2)
        "Estres alto" -> Color(0xFFFFCDD2)
        else -> Color(0xFFE0E0E0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estado actual",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusLabel ?: "Sin datos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                if (!explanation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = explanation,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = "${soilMoisture ?: "--"}%",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
                Text(
                    text = "humedad",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavCard(emoji: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ReadingItem(reading: ReadingDto) {
    val moisture = reading.soilMoisture
    val color = when {
        moisture >= 60 -> Color(0xFF2E7D32)
        moisture >= 30 -> Color(0xFFE65100)
        else -> Color(0xFFB71C1C)
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${moisture}%",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = reading.readAt?.take(10) ?: "—",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { moisture / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
    }
}

@Preview(showBackground = true)
@Composable
private fun PlantDetailContentPreview() {
    PlantCareAppTheme {
        PlantDetailContent(
            plant = PlantDetailDto(
                id = "1",
                name = "Albahaca",
                species = "Ocimum basilicum",
                location = "Balcón",
                imageUrl = null,
                sensorId = null,
                lastWateringAt = null
            ),
            readings = listOf(
                ReadingDto(id = "1", sensorId = "s1", soilMoisture = 65, readAt = "2026-05-09T10:00:00.000Z", source = "sensor"),
                ReadingDto(id = "2", sensorId = "s1", soilMoisture = 28, readAt = "2026-05-08T10:00:00.000Z", source = "sensor"),
                ReadingDto(id = "3", sensorId = "s1", soilMoisture = 45, readAt = "2026-05-07T10:00:00.000Z", source = "sensor"),
            ),
            status = PlantStatusDto(
                statusLabel = "Saludable",
                urgency = "low",
                explanation = "La planta está en buen estado. Mantené el riego actual."
            ),
            onBack = {}
        )
    }
}
