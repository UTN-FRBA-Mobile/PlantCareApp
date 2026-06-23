package com.example.plant_care_app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.plant_care_app.R
import com.example.plant_care_app.data.PlantImageStore
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.models.GardenerAdviceRequest
import com.example.plant_care_app.ui.models.PlantDetailDto
import com.example.plant_care_app.ui.models.PlantSpeciesDto
import com.example.plant_care_app.ui.models.PlantStatusDto
import com.example.plant_care_app.ui.models.ReadingDto
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import com.example.plant_care_app.utils.PlantImageFileManager
import com.example.plant_care_app.utils.PlantImageResolver
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlantDetailScreen(plantId: String, navController: NavController) {

    var plant by remember { mutableStateOf<PlantDetailDto?>(null) }
    var readings by remember { mutableStateOf<List<ReadingDto>>(emptyList()) }
    var status by remember { mutableStateOf<PlantStatusDto?>(null) }
    var gardenerMessage by remember(plantId) { mutableStateOf("") }
    var isGardenerLoading by remember(plantId) { mutableStateOf(false) }
    var gardenerResponse by remember(plantId) { mutableStateOf<String?>(null) }
    var gardenerError by remember(plantId) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(plantId) {
        plant = RetrofitClient.plantApi.getPlantById(plantId)
        readings = RetrofitClient.plantApi.getReadings(plantId).sortedByDescending { it.readAt }
        status = RetrofitClient.plantApi.getPlantStatus(plantId)
    }

    PlantDetailContent(
        plant = plant,
        readings = readings,
        status = status,
        gardenerMessage = gardenerMessage,
        isGardenerLoading = isGardenerLoading,
        gardenerResponse = gardenerResponse,
        gardenerError = gardenerError,
        onGardenerMessageChange = { gardenerMessage = it },
        onGardenerConsult = { message ->
            scope.launch {
                isGardenerLoading = true
                gardenerResponse = null
                gardenerError = null

                try {
                    val response = RetrofitClient.plantApi.getGardenerAdvice(
                        plantId = plantId,
                        body = GardenerAdviceRequest(
                            userMessage = message?.trim()?.takeIf { it.isNotEmpty() }
                        )
                    )
                    gardenerResponse = response.advice
                } catch (e: Exception) {
                    gardenerError = "No se pudo consultar al Jardinero Virtual"
                } finally {
                    isGardenerLoading = false
                }
            }
        },
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
    gardenerMessage: String,
    isGardenerLoading: Boolean,
    gardenerResponse: String?,
    gardenerError: String?,
    onGardenerMessageChange: (String) -> Unit,
    onGardenerConsult: (String?) -> Unit,
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

            if (plant != null) {
                item {
                    VirtualGardenerCard(
                        message = gardenerMessage,
                        isLoading = isGardenerLoading,
                        response = gardenerResponse,
                        error = gardenerError,
                        plantStatusLabel = status?.statusLabel,
                        onMessageChange = onGardenerMessageChange,
                        onConsult = onGardenerConsult
                    )
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

@Composable
private fun VirtualGardenerCard(
    message: String,
    isLoading: Boolean,
    response: String?,
    error: String?,
    plantStatusLabel: String?,
    onMessageChange: (String) -> Unit,
    onConsult: (String?) -> Unit
) {
    val quickActions = listOf(
        "🔍" to "Evaluá mi planta",
        "💧" to "¿Necesita agua?",
        "🍃" to "¿Cómo la ves?",
        "💡" to "¿Qué puedo mejorar?"
    )
    var selectedAction by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color.White,
                                    Color(0xFFDDEFD8)
                                )
                            )
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.jardi_1_saludando),
                        contentDescription = "Jardi saludando",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Text(
                text = "🌱 Contame qué le pasa a tu planta o probá alguna de estas preguntas:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32)
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                quickActions.chunked(2).forEach { actions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        actions.forEach { (emoji, action) ->
                            FilterChip(
                                selected = selectedAction == action,
                                onClick = {
                                    selectedAction = if (selectedAction == action) {
                                        null
                                    } else {
                                        action
                                    }
                                },
                                label = {
                                    Text(
                                        text = action,
                                        fontSize = 12.sp,
                                        lineHeight = 15.sp,
                                        textAlign = TextAlign.Start,
                                        maxLines = 2
                                    )
                                },
                                leadingIcon = {
                                    Text(text = emoji, fontSize = 18.sp)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                enabled = !isLoading
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                placeholder = { Text("Ej: Las hojas están amarillas y la regué ayer") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            GardenerPhotoUploadPreview()

            Text(
                text = "Cada consulta genera un análisis nuevo. Jardi revisará el estado y el historial de tu planta, así que contale todos los detalles que quieras que tenga en cuenta.",
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = Color(0xFF617064)
            )

            Button(
                onClick = {
                    // Combina la acción seleccionada con los detalles escritos antes de enviar una única consulta
                    val writtenDetails = message.trim().takeIf { it.isNotEmpty() }
                    val consultation = when {
                        selectedAction != null && writtenDetails != null -> {
                            "$selectedAction\n\nInformación adicional: $writtenDetails"
                        }
                        selectedAction != null -> selectedAction
                        else -> writtenDetails
                    }
                    onConsult(consultation)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                if (isLoading) {
                    Text("Consultando...")
                } else {
                    Text("Consultar")
                }
            }

            if (isLoading) {
                GardenerThinkingAnimation()
            }

            if (!response.isNullOrBlank()) {
                GardenerResponsePanel(
                    response = response,
                    plantStatusLabel = plantStatusLabel
                )
            }

            if (!error.isNullOrBlank()) {
                Text(
                    text = error,
                    fontSize = 13.sp,
                    color = Color(0xFFB71C1C)
                )
            }
        }
    }
}

@Composable
private fun GardenerPhotoUploadPreview() {
    // Vista anticipada: la carga real de fotos se implementará en una fase posterior
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Fotos para Jardi (opcional)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF526354)
            )
            Text(
                text = "0/3",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.62f))
                        .border(
                            width = 1.dp,
                            color = Color(0xFFB8C9BA),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📷", fontSize = 19.sp)
                        Text(
                            text = "+ Foto",
                            fontSize = 11.sp,
                            color = Color(0xFF617064)
                        )
                    }
                }
            }
        }

        Text(
            text = "Carga de fotos disponible próximamente",
            fontSize = 10.sp,
            color = Color(0xFF7A877C)
        )
    }
}

@Composable
// Muestra en loop los frames de Jardi mientras el backend prepara la respuesta
private fun GardenerThinkingAnimation() {
    val frames = remember {
        listOf(
            R.drawable.jardi_1_evaluando_1,
            R.drawable.jardi_1_evaluando_2,
            R.drawable.jardi_1_evaluando_3
        )
    }
    var frameIndex by remember { mutableStateOf(0) }

    // Recorre los tres frames mientras la consulta está en curso
    LaunchedEffect(Unit) {
        while (true) {
            delay(600)
            frameIndex = (frameIndex + 1) % frames.size
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Crossfade muestra un frame por vez y suaviza la transición entre la imagen anterior y la siguiente
        Crossfade(
            targetState = frameIndex,
            animationSpec = tween(durationMillis = 180),
            label = "gardenerEvaluationFrames"
        ) { index ->
            Image(
                painter = painterResource(frames[index]),
                contentDescription = "Jardi analizando la planta",
                modifier = Modifier
                    .size(width = 150.dp, height = 180.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )
        }
        Text(
            text = "Jardi está analizando tu planta...",
            fontSize = 12.sp,
            color = Color(0xFF526354)
        )
    }
}

@Composable
// Destaca el resultado con el estado detectado, su ilustración y la respuesta de Jardi
private fun GardenerResponsePanel(
    response: String,
    plantStatusLabel: String?
) {
    // El estado actual define el mensaje, los colores y la ilustración del resultado
    val (statusText, statusColor, statusBackground) = when (plantStatusLabel) {
        "Saludable" -> Triple(
            "🟢 Todo bien",
            Color(0xFF2E7D32),
            Color(0xFFE8F5E9)
        )
        "Estres moderado", "Estrés moderado" -> Triple(
            "🟡 Requiere atención",
            Color(0xFF8A5A00),
            Color(0xFFFFF3CD)
        )
        "Estres alto", "Estrés alto" -> Triple(
            "🔴 Acción urgente",
            Color(0xFFB71C1C),
            Color(0xFFFFEBEE)
        )
        else -> Triple(
            "🔵 Seguimiento recomendado",
            Color(0xFF1565C0),
            Color(0xFFE3F2FD)
        )
    }
    val statusImage = when (plantStatusLabel) {
        "Saludable" -> R.drawable.jardi_1_evaluacion_positiva
        "Estres moderado", "Estrés moderado" -> R.drawable.jardi_1_evaluacion_requiereatencion
        "Estres alto", "Estrés alto" -> R.drawable.jardi_1_evaluacion_urgente
        else -> R.drawable.jardi_1_evaluacion_seguimiento
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.82f))
            .border(
                width = 1.dp,
                color = Color(0xFFA5C5A8),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "🌱", fontSize = 18.sp)
            Text(
                text = "Respuesta de Jardi",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
        }
        HorizontalDivider(color = Color(0xFFD6E5D7))
        Text(
            text = statusText,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(statusBackground)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
        Image(
            painter = painterResource(statusImage),
            contentDescription = "Jardi mostrando el resultado: $statusText",
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            contentScale = ContentScale.Fit
        )
        GardenerAdviceText(response)
    }
}

@Composable
// Convierte la respuesta del backend en texto legible respetando el formato Markdown simple
private fun GardenerAdviceText(advice: String) {
    val formattedAdvice = remember(advice) {
        parseGardenerAdvice(advice)
    }

    Text(
        text = formattedAdvice,
        fontSize = 13.sp,
        color = Color(0xFF25382A),
        lineHeight = 20.sp
    )
}

// Interpreta únicamente el formato Markdown simple que puede devolver el Jardinero
private fun parseGardenerAdvice(advice: String): AnnotatedString {
    val headingPattern = Regex("^\\s*#{1,6}\\s+(.+)$")
    val bulletPattern = Regex("^\\s*[-*]\\s+(.+)$")
    val lines = advice.lines()

    return buildAnnotatedString {
        lines.forEachIndexed { index, line ->
            val heading = headingPattern.matchEntire(line)?.groupValues?.get(1)
            val bullet = bulletPattern.matchEntire(line)?.groupValues?.get(1)

            when {
                heading != null -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 15.sp)) {
                        appendSimpleBold(heading)
                    }
                }
                bullet != null -> {
                    append("• ")
                    appendSimpleBold(bullet)
                }
                else -> appendSimpleBold(line)
            }

            if (index < lines.lastIndex) {
                append('\n')
            }
        }
    }
}

private fun AnnotatedString.Builder.appendSimpleBold(text: String) {
    var currentIndex = 0

    while (currentIndex < text.length) {
        val boldStart = text.indexOf("**", currentIndex)
        if (boldStart == -1) {
            append(text.substring(currentIndex))
            return
        }

        val boldEnd = text.indexOf("**", boldStart + 2)
        if (boldEnd == -1) {
            append(text.substring(currentIndex))
            return
        }

        append(text.substring(currentIndex, boldStart))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(text.substring(boldStart + 2, boldEnd))
        }
        currentIndex = boldEnd + 2
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
            gardenerMessage = "",
            isGardenerLoading = false,
            gardenerResponse = null,
            gardenerError = null,
            onGardenerMessageChange = {},
            onGardenerConsult = { _ -> },
            onBack = {},
            onEvaluationsClick = { _, _, _ -> }
        )
    }
}
