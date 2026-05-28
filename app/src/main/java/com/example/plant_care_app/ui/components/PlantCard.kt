package com.example.plant_care_app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.plant_care_app.R
import java.io.File

@Composable
fun PlantCard(
    name: String,
    location: String,
    humidity: Int,
    status: String,
    imageUrl: String?,
    onClick: () -> Unit
) {
    val backgroundColor = when (status) {

        "Saludable" -> Color(0xFFC8E6C9)

        "Estres moderado" -> Color(0xFFFFE0B2)

        "Estres alto" -> Color(0xFFFFCDD2)

        else -> Color(0xFFE0E0E0)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )

    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {

            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = File(imageUrl),

                    contentDescription = "Planta",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.planta),
                    contentDescription = "Planta",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(text = location)

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = status)
            }

            Text(
                text = "$humidity%",
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PlantCardPreview() {
    PlantCard(
        "Clotilde",
        "Balcón detrás",
        62,
        "Estoy Bien!",
        null
    ) {}
}
