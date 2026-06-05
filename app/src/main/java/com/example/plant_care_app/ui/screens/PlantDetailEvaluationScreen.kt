package com.example.plant_care_app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.models.ReadingDto
import com.example.plant_care_app.ui.theme.PlantCareAppTheme
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Screen that shows real detailed evaluation of a plant using its readings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailEvaluationScreen(
    navController: NavController,
    plantId: String = "",
    plantName: String = "Monstera",
    plantType: String = "Tropical"
) {
    var readings by remember { mutableStateOf<List<ReadingDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(plantId) {
        if (plantId.isNotEmpty()) {
            try {
                isLoading = true
                val response = RetrofitClient.plantApi.getReadings(plantId)
                readings = response
                errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Error al cargar las evaluaciones"
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle de Planta",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else if (errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(text = errorMessage!!, color = Color.Red, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Prominent and Centered Plant Info
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = plantName,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = plantType,
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (readings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no hay lecturas disponibles para esta planta.",
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Main Evaluations Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Text(
                                    text = "Evaluaciones",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Simple Humidity Line Chart (Oldest to Newest)
                                val chartData = readings.sortedBy { it.readAt }.map { it.soilMoisture.toFloat() }
                                HumidityLineChart(
                                    data = chartData,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(16.dp)
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )

                                Text(
                                    text = "Historial de Evaluaciones",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                // Real Chronological Evaluation History (Newest First)
                                val historyReadings = readings.sortedByDescending { it.readAt }
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    historyReadings.forEach { reading ->
                                        EvaluationHistoryItem(reading)
                                    }
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * A simple line chart to visualize humidity evolution.
 */
@Composable
fun HumidityLineChart(data: List<Float>, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val labelColor = Color.Gray.toArgb()

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val maxVal = 100f
            val leftPadding = 60f
            val bottomPadding = 20f
            val chartWidth = width - leftPadding
            val chartHeight = height - bottomPadding

            // Draw Y-axis labels (0, 50, 100)
            val paint = android.graphics.Paint().apply {
                color = labelColor
                textSize = 28f
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText("100%", leftPadding - 10f, 25f, paint)
            drawContext.canvas.nativeCanvas.drawText("50%", leftPadding - 10f, chartHeight / 2 + 10f, paint)
            drawContext.canvas.nativeCanvas.drawText("0%", leftPadding - 10f, chartHeight, paint)

            // Draw simple grid lines
            drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(leftPadding, 15f), Offset(width, 15f))
            drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(leftPadding, chartHeight / 2), Offset(width, chartHeight / 2))
            drawLine(Color.LightGray.copy(alpha = 0.3f), Offset(leftPadding, chartHeight), Offset(width, chartHeight))

            // Draw Line
            if (data.isNotEmpty()) {
                val path = Path()
                data.forEachIndexed { index, value ->
                    val x = leftPadding + index * chartWidth / (if (data.size > 1) data.size - 1 else 1)
                    val y = chartHeight - (value / maxVal * chartHeight)
                    
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }

                    // Draw dots for each value
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

/**
 * Displays a single row in the evaluation history with specific coloring for recommendations.
 */
@Composable
private fun EvaluationHistoryItem(reading: ReadingDto) {
    val moisture = reading.soilMoisture
    
    val recommendation = when {
        moisture >= 70 -> "NO REGAR"
        moisture >= 50 -> "REVISAR"
        else -> "REGAR"
    }

    val recommendationColor = when (recommendation) {
        "NO REGAR" -> Color(0xFF2E7D32) // Green
        "REVISAR" -> Color(0xFFE65100) // Orange
        "REGAR" -> Color(0xFFB71C1C)   // Red
        else -> Color.Gray
    }

    // Format the date and time from readAt
    val formattedDateTime = remember(reading.readAt) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
            val date = reading.readAt?.let { inputFormat.parse(it) }
            date?.let { outputFormat.format(it) } ?: reading.readAt ?: "—"
        } catch (e: Exception) {
            reading.readAt?.take(16)?.replace("T", " ") ?: "—"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$formattedDateTime - $moisture% - ",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = recommendation,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = recommendationColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlantDetailEvaluationScreenPreview() {
    PlantCareAppTheme {
        PlantDetailEvaluationScreen(navController = rememberNavController())
    }
}
