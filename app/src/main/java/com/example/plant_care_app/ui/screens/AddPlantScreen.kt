package com.example.plant_care_app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plant_care_app.data.CloudinaryUploader
import com.example.plant_care_app.data.PlantImageStore
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.models.PlantIdentificationCandidateDto
import com.example.plant_care_app.ui.models.PlantSpeciesDto
import com.example.plant_care_app.ui.models.SensorDto
import com.example.plant_care_app.utils.PlantImageFileManager
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private val GreenPrimary = Color(0xFF2E7D32)
private val GreenLight = Color(0xFFA5D6A7)
private const val OtherSpeciesId = "other"

@Composable
fun AddPlantScreen(
    onBack: () -> Unit = {},
    onAddSensor: () -> Unit = {},
    onPlantCreated: (String) -> Unit = {},
    linkedSensorId: String? = null,
    onLinkedSensorHandled: () -> Unit = {}
) {
    val locations = listOf("Sala", "Balcón", "Living", "Habitación", "Patio", "Terraza", "Cocina")

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var photoUriValue by rememberSaveable { mutableStateOf<String?>(null) }
    var photoFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    val photoUri = photoUriValue?.let(Uri::parse)
    val photoFile = photoFilePath?.let(::File)

    var sensors by remember { mutableStateOf<List<SensorDto>>(emptyList()) }
    var speciesCatalog by remember { mutableStateOf<List<PlantSpeciesDto>>(emptyList()) }
    var identifiedCandidates by remember { mutableStateOf<List<PlantIdentificationCandidateDto>>(emptyList()) }
    var name by rememberSaveable { mutableStateOf("") }
    var selectedSpeciesName by rememberSaveable { mutableStateOf("") }
    var selectedSpeciesId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedLocation by rememberSaveable { mutableStateOf("") }
    var selectedSensorName by rememberSaveable { mutableStateOf("") }
    var selectedSensorId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCandidateScientificName by rememberSaveable { mutableStateOf<String?>(null) }
    var identifiedCommonName by rememberSaveable { mutableStateOf<String?>(null) }
    var identifiedScientificName by rememberSaveable { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isIdentifyingSpecies by remember { mutableStateOf(false) }
    var identificationMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun normalizeSpeciesName(value: String): String =
        value.lowercase(Locale.ROOT).trim()

    // Cruza un candidato identificado con las especies del catálogo local.
    fun PlantSpeciesDto.matches(candidate: PlantIdentificationCandidateDto): Boolean {
        val candidateNames = listOfNotNull(candidate.scientificName, candidate.commonName)
            .map(::normalizeSpeciesName)
        val catalogNames = (listOf(displayName) + aliases.orEmpty())
            .map(::normalizeSpeciesName)

        return candidateNames.any { candidateName ->
            catalogNames.any { catalogName ->
                candidateName == catalogName ||
                    candidateName.contains(catalogName) ||
                    catalogName.contains(candidateName)
            }
        }
    }

    // Sube la foto al endpoint de identificación y preselecciona la especie más cercana.
    fun PlantIdentificationCandidateDto.title(): String =
        displayName?.takeIf { it.isNotBlank() }
            ?: commonName?.takeIf { it.isNotBlank() }
            ?: scientificName

    fun selectIdentifiedCandidate(candidate: PlantIdentificationCandidateDto) {
        val speciesId = candidate.matchedSpeciesId ?: OtherSpeciesId
        val speciesName = candidate.displayName
            ?: speciesCatalog.firstOrNull { it.id == speciesId }?.displayName
            ?: candidate.title()

        selectedSpeciesId = speciesId
        selectedSpeciesName = speciesName
        selectedCandidateScientificName = candidate.scientificName

        if (candidate.matchedSpeciesId == null) {
            identifiedCommonName = candidate.commonName
            identifiedScientificName = candidate.scientificName
        } else {
            identifiedCommonName = null
            identifiedScientificName = null
        }

        identificationMessage = "Sugerencia por foto: $speciesName"
    }

    fun identifySpeciesFromPhoto(file: File) {
        isIdentifyingSpecies = true
        identificationMessage = null
        identifiedCandidates = emptyList()

        scope.launch {
            try {
                val image = MultipartBody.Part.createFormData(
                    name = "image",
                    filename = file.name,
                    body = file.asRequestBody("image/jpeg".toMediaType())
                )
                val response = RetrofitClient.plantApi.identifySpecies(
                    image = image,
                    organ = "leaf".toRequestBody("text/plain".toMediaType())
                )
                val candidates = (listOfNotNull(response.bestMatch) + response.candidates)
                    .distinctBy { normalizeSpeciesName(it.scientificName) }
                    .sortedByDescending { it.score ?: 0.0 }

                identifiedCandidates = candidates
                val suggestedCandidate = candidates.firstOrNull()

                if (suggestedCandidate != null) {
                    selectIdentifiedCandidate(suggestedCandidate)
                } else {
                    identificationMessage = "No se encontraron especies posibles para la foto."
                }
            } catch (e: Exception) {
                identificationMessage = "No se pudo identificar la planta desde la foto."
            } finally {
                isIdentifyingSpecies = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoFilePath?.let { path ->
                val file = File(path)
                photoUriValue = PlantImageFileManager.getUriForFile(context, file).toString()
                identifySpeciesFromPhoto(file)
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUriValue = uri.toString()
            val copiedFile = PlantImageFileManager.copyUriToPlantImageFile(context, uri)
            photoFilePath = copiedFile?.absolutePath
            copiedFile?.let(::identifySpeciesFromPhoto)
        }
    }

    fun launchCamera() {
        val file = PlantImageFileManager.createImageFile(context)
        val uri = PlantImageFileManager.getUriForFile(context, file)

        photoFilePath = file.absolutePath
        cameraLauncher.launch(uri)
    }

    suspend fun loadFormData(sensorIdToSelect: String? = null) {
        try {
            val loadedSensors = RetrofitClient.plantApi.getSensors()
            sensors = loadedSensors
            speciesCatalog = RetrofitClient.plantApi.getSpecies()

            if (!sensorIdToSelect.isNullOrBlank()) {
                loadedSensors.firstOrNull { it.id == sensorIdToSelect }?.let { sensor ->
                    selectedSensorName = sensor.name
                    selectedSensorId = sensor.id
                }
                onLinkedSensorHandled()
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Error al cargar datos del formulario."
        }
    }

    LaunchedEffect(linkedSensorId) {
        loadFormData(linkedSensorId)
    }

    // Cuando el catálogo llega después de la identificación, intenta aplicar la sugerencia pendiente.
    LaunchedEffect(speciesCatalog, identifiedCandidates) {
        if (selectedSpeciesId.isNullOrBlank() && speciesCatalog.isNotEmpty() && identifiedCandidates.isNotEmpty()) {
            selectIdentifiedCandidate(identifiedCandidates.first())
        }
    }

    val speciesOptions = speciesCatalog.distinctBy { it.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = GreenPrimary
                )
            }
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = "Nueva planta",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(200.dp, 170.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { launchCamera() }
                .drawBehind {
                    if (photoUri == null) {
                        drawRoundRect(
                            color = GreenLight,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                            ),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    }
                }
        ) {
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Foto de la planta",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📷", fontSize = 40.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tomar foto", color = GreenLight, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (photoUri != null) "Tocá para cambiar la foto" else "Tocá para usar la cámara",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(28.dp))

        IdentificationCandidates(
            candidates = identifiedCandidates.take(3),
            isLoading = isIdentifyingSpecies,
            message = identificationMessage,
            selectedScientificName = selectedCandidateScientificName,
            onCandidateSelected = ::selectIdentifiedCandidate
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Especie", fontWeight = FontWeight.SemiBold, color = GreenPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            PlantDropdown(
                label = "",
                placeholder = if (speciesCatalog.isEmpty()) "Cargando especies..." else "Seleccionar especie",
                options = speciesOptions.map { it.displayName },
                selected = selectedSpeciesName,
                showLabel = false,
                onSelected = { displayName ->
                    selectedSpeciesName = displayName
                    selectedSpeciesId = speciesOptions.find { it.displayName == displayName }?.id
                    selectedCandidateScientificName = null
                    identifiedCommonName = null
                    identifiedScientificName = null
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row {
                Text("Nombre", fontWeight = FontWeight.SemiBold, color = GreenPrimary)
                Text(" *", color = Color.Red)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Ej: Monstera, Albahaca...", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.LightGray
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        PlantDropdown(
            label = "Ubicación",
            placeholder = "Seleccionar ubicación",
            options = locations,
            selected = selectedLocation,
            onSelected = { selectedLocation = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            PlantDropdown(
                label = "Sensor (opcional)",
                placeholder = "Sin sensor",
                options = listOf("Sin sensor") + sensors.filter { it.status == "AVAILABLE" }.map { it.name },
                selected = selectedSensorName,
                onSelected = { name ->
                    selectedSensorName = name
                    selectedSensorId = sensors.find { it.name == name }?.id
                }
            )
            OutlinedButton(
                onClick = onAddSensor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GreenPrimary),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = GreenPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Vincular sensor con codigo",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                if (name.isBlank()) {
                    errorMessage = "El nombre es obligatorio."
                    return@Button
                }
                if (selectedSpeciesId.isNullOrBlank()) {
                    errorMessage = "Selecciona una especie."
                    return@Button
                }
                if (selectedLocation.isBlank()) {
                    errorMessage = "Seleccioná una ubicación."
                    return@Button
                }
                errorMessage = null
                isLoading = true
                scope.launch {
                    try {
                        // Sube la foto a Cloudinary y obtiene la URL remota a persistir.
                        val imageUrl = photoFile?.let { file ->
                            CloudinaryUploader.upload(file)
                        }

                        val createdPlant = RetrofitClient.plantApi.createPlant(
                            name = name.trim().toRequestBody("text/plain".toMediaType()),
                            speciesId = selectedSpeciesId!!.toRequestBody("text/plain".toMediaType()),
                            location = selectedLocation.toRequestBody("text/plain".toMediaType()),
                            imageUrl = imageUrl?.toRequestBody("text/plain".toMediaType()),
                            sensorId = selectedSensorId
                                ?.takeIf { it.isNotBlank() }
                                ?.toRequestBody("text/plain".toMediaType()),
                            identifiedCommonName = identifiedCommonName
                                ?.takeIf { selectedSpeciesId == OtherSpeciesId && it.isNotBlank() }
                                ?.toRequestBody("text/plain".toMediaType()),
                            identifiedScientificName = identifiedScientificName
                                ?.takeIf { selectedSpeciesId == OtherSpeciesId && it.isNotBlank() }
                                ?.toRequestBody("text/plain".toMediaType())
                        )

                        photoFile?.let { file ->
                            PlantImageStore.saveImagePath(
                                context = context,
                                plantId = createdPlant.id,
                                imagePath = file.absolutePath
                            )
                        }

                        onPlantCreated(createdPlant.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessage = e.message ?: "Error al guardar. Intentá de nuevo."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
        ) {
            if (isLoading) {
                Text(text = "Guardando...", fontSize = 16.sp, color = Color.White)
            } else {
                Text(text = "🌱", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Guardar planta",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun IdentificationCandidates(
    candidates: List<PlantIdentificationCandidateDto>,
    isLoading: Boolean,
    message: String?,
    selectedScientificName: String?,
    onCandidateSelected: (PlantIdentificationCandidateDto) -> Unit
) {
    // Muestra el estado de identificación y las tres mejores posibilidades detectadas.
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Posibles especies", fontWeight = FontWeight.SemiBold, color = GreenPrimary)
        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    color = GreenPrimary,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Identificando especie desde la foto...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            return
        }

        if (candidates.isEmpty()) {
            Text(
                text = message ?: "Cargá una foto para ver sugerencias.",
                fontSize = 12.sp,
                color = Color.Gray
            )
            return
        }

        candidates.forEachIndexed { index, candidate ->
            CandidateBadge(
                rank = index + 1,
                candidate = candidate,
                isSelected = candidate.scientificName == selectedScientificName,
                onClick = { onCandidateSelected(candidate) }
            )
            if (index < candidates.lastIndex) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (!message.isNullOrBlank()) {
            Text(
                text = message,
                fontSize = 12.sp,
                color = if (!selectedScientificName.isNullOrBlank()) GreenPrimary else Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun CandidateBadge(
    rank: Int,
    candidate: PlantIdentificationCandidateDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Badge visual para cada candidato, con ranking y porcentaje de confianza.
    val score = candidate.score?.let { "${(it * 100).toInt()}%" } ?: "--"
    val name = candidate.displayName?.takeIf { it.isNotBlank() }
        ?: candidate.commonName?.takeIf { it.isNotBlank() }
        ?: candidate.scientificName

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFDFF2E1) else Color(0xFFEAF5EA))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            color = GreenPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                color = Color(0xFF263238),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = candidate.scientificName,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = score,
            color = GreenPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantDropdown(
    label: String,
    placeholder: String,
    options: List<String>,
    selected: String,
    showLabel: Boolean = true,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showLabel) {
            Text(label, fontWeight = FontWeight.SemiBold, color = GreenPrimary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(placeholder, color = Color.LightGray) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = GreenPrimary,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddPlantScreenPreview() {
    AddPlantScreen()
}
