package com.example.plant_care_app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.models.CreateSensorRequest
import com.example.plant_care_app.ui.models.UpdateSensorRequest
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSensorScreen(
    sensorId: String?,
    onBack: () -> Unit,
) {
    val isEditMode = sensorId != null
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var name by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(isEditMode) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (isEditMode) {
        LaunchedEffect(sensorId) {
            try {
                val sensor = RetrofitClient.plantApi.getSensorById(sensorId!!)
                name = sensor.name
                apiKey = sensor.apiKey
            } catch (e: Exception) {
                errorMessage = "Error al cargar el sensor"
            } finally {
                isFetching = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Editar sensor" else "Nuevo sensor",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        if (isFetching) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                Text("Nombre", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.DarkGray)
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Sensor del patio") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                )

                if (isEditMode && apiKey != null) {
                    Spacer(Modifier.height(24.dp))
                    Text("API Key", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = apiKey!!,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color.LightGray,
                            disabledTextColor = Color.Gray,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("apiKey", apiKey))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = Color(0xFF2E7D32))
                            }
                        }
                    )
                    Text(
                        "Usá esta clave para configurar tu dispositivo IoT.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                errorMessage?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = Color(0xFFD32F2F), fontSize = 13.sp)
                }

                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "El nombre es obligatorio."
                            return@Button
                        }
                        keyboardController?.hide()
                        isLoading = true
                        scope.launch {
                            try {
                                if (isEditMode) {
                                    RetrofitClient.plantApi.updateSensor(sensorId!!, UpdateSensorRequest(name.trim()))
                                } else {
                                    RetrofitClient.plantApi.createSensor(CreateSensorRequest(name.trim()))
                                }
                                onBack()
                            } catch (e: HttpException) {
                                errorMessage = "Error al guardar. Intentá de nuevo."
                            } catch (e: Exception) {
                                errorMessage = "Error al guardar. Intentá de nuevo."
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(end = 8.dp).height(20.dp), strokeWidth = 2.dp)
                        Text("Guardando...")
                    } else {
                        Text(if (isEditMode) "Guardar cambios" else "Crear sensor", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
