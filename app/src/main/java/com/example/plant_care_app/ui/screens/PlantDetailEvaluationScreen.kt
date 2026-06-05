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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.plant_care_app.ui.theme.PlantCareAppTheme

/**
 * Refined Screen that shows the detailed evaluation of a plant.
 * Includes a custom simple line chart for humidity evolution.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailEvaluationScreen(
    navController: NavController,
    plantName: String = "Monstera",
    plantType: String = "Tropical"
) {
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

                        // Simple Humidity Line Chart
                        HumidityLineChart(
                            data = listOf(70f, 68f, 62f, 25f, 45f),
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

                        // Chronological Evaluation History (Newest First)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            EvaluationHistoryItem("03/04/2026", "09:00 AM", "45%", "REVISAR")
                            EvaluationHistoryItem("02/04/2026", "09:00 AM", "25%", "REGAR")
                            EvaluationHistoryItem("01/04/2026", "09:00 AM", "62%", "NO REGAR")
                            EvaluationHistoryItem("31/03/2026", "10:30 AM", "68%", "NO REGAR")
                            EvaluationHistoryItem("30/03/2026", "04:15 PM", "70%", "NO REGAR")
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
private fun EvaluationHistoryItem(
    date: String,
    time: String,
    value: String,
    recommendation: String
) {
    val recommendationColor = when (recommendation) {
        "NO REGAR" -> Color(0xFF2E7D32) // Green
        "REVISAR" -> Color(0xFFE65100) // Orange
        "REGAR" -> Color(0xFFB71C1C)   // Red
        else -> Color.Gray
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$date, $time - $value - ",
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
