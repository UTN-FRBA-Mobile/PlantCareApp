package com.example.plant_care_app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.plant_care_app.R
import com.example.plant_care_app.utils.PlantImageResolver

@Composable
fun PlantCard(
    plantId: String,
    name: String,
    location: String,
    humidity: Int,
    status: String,
    imageUrl: String?,
    sensorName: String?,
    hasSensor: Boolean,
    onClick: () -> Unit
) {
    val statusStyle = plantStatusStyle(status)
    val context = LocalContext.current
    val imageModel = PlantImageResolver.resolve(
        context = context,
        plantId = plantId,
        imageUrl = imageUrl
    )

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, statusStyle.borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = imageModel ?: R.drawable.planta,
                contentDescription = "Planta",
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = name,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = location,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    StatusChip(
                        label = status,
                        dotColor = statusStyle.dotColor,
                        containerColor = statusStyle.chipColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (hasSensor) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasSensor) sensorName ?: "Sensor conectado" else "Sin sensor",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = Color(0xFF2589BD),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$humidity%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { (humidity.coerceIn(0, 100)) / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = statusStyle.progressColor,
                    trackColor = Color(0xFFE8EFE5)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    dotColor: Color,
    containerColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private data class PlantStatusStyle(
    val borderColor: Color,
    val chipColor: Color,
    val dotColor: Color,
    val progressColor: Color
)

private fun plantStatusStyle(status: String): PlantStatusStyle {
    val normalized = status.lowercase()

    return when {
        "saludable" in normalized -> PlantStatusStyle(
            borderColor = Color(0xFF8BCB8F),
            chipColor = Color(0xFFEAF7EA),
            dotColor = Color(0xFF2E7D32),
            progressColor = Color(0xFF2E7D32)
        )
        "moderado" in normalized -> PlantStatusStyle(
            borderColor = Color(0xFFE9B65F),
            chipColor = Color(0xFFFFF4DD),
            dotColor = Color(0xFFF9A825),
            progressColor = Color(0xFFE69A18)
        )
        "alto" in normalized -> PlantStatusStyle(
            borderColor = Color(0xFFE58C89),
            chipColor = Color(0xFFFFECEB),
            dotColor = Color(0xFFD32F2F),
            progressColor = Color(0xFFD32F2F)
        )
        else -> PlantStatusStyle(
            borderColor = Color(0xFFD6DDD2),
            chipColor = Color(0xFFF0F3EF),
            dotColor = Color(0xFF7B8578),
            progressColor = Color(0xFF7B8578)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlantCardPreview() {
    PlantCard(
        plantId = "1",
        name = "Clotilde",
        location = "Balcon detras",
        humidity = 62,
        status = "Estres moderado",
        imageUrl = null,
        sensorName = "Sensor 1",
        hasSensor = true
    ) {}
}
