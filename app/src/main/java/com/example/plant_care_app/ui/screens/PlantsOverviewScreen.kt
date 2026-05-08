package com.example.plant_care_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plant_care_app.data.RetrofitClient
import com.example.plant_care_app.ui.components.PlantCard
import com.example.plant_care_app.ui.models.PlantOverviewDto
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.plant_care_app.R

data class PlantUi(
    val name: String,
    val location: String,
    val humidity: Int,
    val status: String
)

@Composable
fun PlantsOverviewScreen() {

    var plants by remember { mutableStateOf<List<PlantOverviewDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        plants = RetrofitClient.plantApi.getOverview()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_image),
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp)
            )



            Text(
                text = "Mis Plantitas",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            val context = LocalContext.current

            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        "Funcionalidad en desarrollo",
                        Toast.LENGTH_LONG
                    ).show()
                },

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {

            items(plants) { plant ->

                PlantCard(
                    name = plant.name,
                    location = plant.location,
                    humidity = plant.soilMoisture ?: 0,
                    status = plant.statusLabel ?: "Sin lecturas"
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlantsOverviewScreenPreview() {
    PlantsOverviewScreen()
}