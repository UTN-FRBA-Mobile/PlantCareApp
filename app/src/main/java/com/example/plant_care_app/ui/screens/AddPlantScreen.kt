package com.example.plant_care_app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plant_care_app.data.PlantImageStore
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.models.PlantSpeciesDto
import com.example.plant_care_app.ui.models.SensorDto
import com.example.plant_care_app.utils.PlantImageFileManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private val GreenPrimary = Color(0xFF2E7D32)
private val GreenLight = Color(0xFFA5D6A7)

@Composable
fun AddPlantScreen(onBack: () -> Unit = {}) {
    val locations = listOf("Sala", "Balcón", "Living", "Habitación", "Parque")

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var photoUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = photoFile?.let { PlantImageFileManager.getUriForFile(context, it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            photoFile = PlantImageFileManager.copyUriToPlantImageFile(context, uri)
        }
    }

    fun launchCamera() {
        val file = PlantImageFileManager.createImageFile(context)
        val uri = PlantImageFileManager.getUriForFile(context, file)

        photoFile = file
        cameraLauncher.launch(uri)
    }

    var sensors by remember { mutableStateOf<List<SensorDto>>(emptyList()) }
    var speciesCatalog by remember { mutableStateOf<List<PlantSpeciesDto>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var selectedSpeciesName by remember { mutableStateOf("") }
    var selectedSpeciesId by remember { mutableStateOf<String?>(null) }
    var selectedLocation by remember { mutableStateOf("") }
    var selectedSensorName by remember { mutableStateOf("") }
    var selectedSensorId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            sensors = RetrofitClient.plantApi.getSensors()
            speciesCatalog = RetrofitClient.plantApi.getSpecies()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Error al cargar datos del formulario."
        }
    }

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

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Especie", fontWeight = FontWeight.SemiBold, color = GreenPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            PlantDropdown(
                label = "",
                placeholder = if (speciesCatalog.isEmpty()) "Cargando especies..." else "Seleccionar especie",
                options = speciesCatalog.map { it.displayName },
                selected = selectedSpeciesName,
                showLabel = false,
                onSelected = { displayName ->
                    selectedSpeciesName = displayName
                    selectedSpeciesId = speciesCatalog.find { it.displayName == displayName }?.id
                }
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

        PlantDropdown(
            label = "Sensor",
            placeholder = "Sin sensor",
            options = listOf("Sin sensor") + sensors.filter { it.status == "AVAILABLE" }.map { it.name },
            selected = selectedSensorName,
            onSelected = { name ->
                selectedSensorName = name
                selectedSensorId = sensors.find { it.name == name }?.id
            }
        )

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
                        // Se obtiene la imagen de la planta
                        val imagePlant = photoFile?.let { file ->
                            val requestFile = file.asRequestBody("image/jpeg".toMediaType())

                            MultipartBody.Part.createFormData(
                                name = "image",
                                filename = file.name,
                                body = requestFile
                            )
                        }

                        val createdPlant = RetrofitClient.plantApi.createPlant(
                            image = imagePlant,
                            name = name.trim().toRequestBody("text/plain".toMediaType()),
                            speciesId = selectedSpeciesId!!.toRequestBody("text/plain".toMediaType()),
                            location = selectedLocation.toRequestBody("text/plain".toMediaType()),
                            sensorId = (selectedSensorId ?: "").toRequestBody("text/plain".toMediaType())
                        )

                        photoFile?.let { file ->
                            PlantImageStore.saveImagePath(
                                context = context,
                                plantId = createdPlant.id,
                                imagePath = file.absolutePath
                            )
                        }

                        onBack()
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
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
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
