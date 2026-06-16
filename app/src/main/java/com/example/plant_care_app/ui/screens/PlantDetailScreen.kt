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
import com.example.plant_care_app.ui.models.PlantSpeciesDto
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
        readings = RetrofitClient.plantApi.getReadings(plantId).sortedByDescending { it.readAt }
        status = RetrofitClient.plantApi.getPlantStatus(plantId)
    }

    PlantDetailContent(
        plant = plant,
        readings = readings,
        status = status,
        onBack = { navController.popBackStack() },
        onEvaluationsClick = { id, name, species ->
            navController.navigate("plant_evaluations/$id/$name/$species")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantDetailContent(
    plant: PlantDetailDto?,
    readings: List<ReadingDto>,
    status: PlantStatusDto?,
    onBack: () -> Unit,
    onEvaluationsClick: (String, String, String) -> Unit
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
                    recommendation = status?.recommendation,
                    soilMoisture = readings.firstOrNull()?.soilMoisture
                )
            }

            plant?.speciesDetails?.let { speciesDetails ->
                item {
                    SpeciesCareCard(speciesDetails)
                }
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
                        onClick = {
                            onEvaluationsClick(plant?.id ?: "", plant?.name ?: "Planta", plant?.species ?: "Especie")
                        }
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
    recommendation: String?,
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
                /*if (!explanation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = explanation,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp
                    )
                }*/
                if (!recommendation.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = recommendation,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor,
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

@Composable
private fun SpeciesCareCard(species: PlantSpeciesDto) {
    val humidityRange = species.humidityMin?.let { min ->
        species.humidityMax?.let { max -> "$min% - $max%" }
    }
    val temperatureRange = species.temperatureMin?.let { min ->
        species.temperatureMax?.let { max -> "$min C - $max C" }
    }
    val careItems = listOfNotNull(
        humidityRange?.let { CareMetric("Humedad", it) },
        temperatureRange?.let { CareMetric("Temperatura", it) },
        species.wateringFrequency?.let { CareMetric("Riego", it) },
        species.lightRequirement?.let { CareMetric("Luz", it) },
        species.difficulty?.let { CareMetric("Dificultad", it) }
    )
    val tips = species.careTips.orEmpty().filter { it.isNotBlank() }.take(3)
    val fact = remember(species.id, species.facts) {
        val facts = species.facts.orEmpty().filter { it.isNotBlank() }
        facts.getOrNull(Math.floorMod(species.id.hashCode(), facts.size.coerceAtLeast(1)))
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SpeciesProfileCard(
            species = species,
            careItems = careItems
        )

        if (tips.isNotEmpty()) {
            CareTipsCard(tips)
        }

        if (!fact.isNullOrBlank()) {
            SpeciesFactCard(fact)
        }
    }
}

private data class CareMetric(
    val label: String,
    val value: String
)

@Composable
private fun SpeciesProfileCard(
    species: PlantSpeciesDto,
    careItems: List<CareMetric>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "${species.emoji.orEmpty()} ${species.displayName}".trim(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            if (!species.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = species.description,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp
                )
            }

            if (careItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    careItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                CareMetricCard(
                                    metric = item,
                                    modifier = if (rowItems.size == 1) {
                                        Modifier.fillMaxWidth()
                                    } else {
                                        Modifier.weight(1f)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CareMetricCard(
    metric: CareMetric,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.78f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = metric.label,
                fontSize = 11.sp,
                color = Color(0xFF6D7D70),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = metric.value,
                fontSize = 13.sp,
                color = Color(0xFF25382A),
                fontWeight = FontWeight.Bold,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun CareTipsCard(tips: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Tips de cuidado",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A5600)
            )
            Spacer(modifier = Modifier.height(10.dp))
            tips.forEachIndexed { index, tip ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${index + 1}.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A5600)
                    )
                    Text(
                        text = tip,
                        fontSize = 13.sp,
                        color = Color(0xFF3D3320),
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeciesFactCard(fact: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Dato curioso",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E4F8A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = fact,
                fontSize = 13.sp,
                color = Color(0xFF26384C),
                lineHeight = 18.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NavCard(emoji: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp)
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 23.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color(0xFF25382A)
            )
        }
    }
}

@Composable
private fun ReadingItem(reading: ReadingDto) {
    val moisture = reading.soilMoisture
    val color = when {
        moisture >= 60 -> Color(0xFF2E7D32)
        moisture >= 30 -> Color(0xFFD86B21)
        else -> Color(0xFFC9472C)
    }

    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${moisture}%",
                fontSize = 18.sp,
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
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.16f)
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
            onBack = {},
            onEvaluationsClick = { _, _, _ -> }
        )
    }
}
