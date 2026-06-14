package com.example.bachewatch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun InicioScreen(
    onAgregarReporteClick: () -> Unit,
    onVerReportesClick: () -> Unit
) {
    var baches by remember { mutableStateOf<List<Bache>>(emptyList()) }

    LaunchedEffect(Unit) {
        FirestoreManager.obtenerBaches(
            onSuccess = { baches = it },
            onFailure = { it.printStackTrace() }
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(19.432608, -99.133209),
            14f
        )
    }

    // El Box permite superponer elementos sobre el mapa
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. EL MAPA OCUPA TODA LA PANTALLA ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            baches.forEach { bache ->
                Marker(
                    state = MarkerState(
                        position = LatLng(bache.latitude, bache.longitude)
                    ),
                    title = bache.title,
                    snippet = bache.description
                )
            }
        }

        // --- 2. TEXTOS FLOTANDO EN LA PARTE SUPERIOR ---
        // Usamos una pequeña tarjeta con transparencia para que el texto resalte sobre el mapa
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 48.dp), // Margen para librar la barra de estado
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "BacheWatch",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Monitoreo y reporte ciudadano de desperfectos viales.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 3. BOTONES FLOTANDO EN LA PARTE INFERIOR ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart) // <-- Cambiado a BottomStart (Abajo a la izquierda)
                .width(260.dp) // <-- Ancho reducido para que ocupe solo "un pedacito"
                .padding(start = 16.dp, bottom = 40.dp), // Margen respecto a los bordes de la pantalla
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Botón: Agregar Reporte (Más compacto)
            Button(
                onClick = onAgregarReporteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp), // Reducido ligeramente de 56 a 48 para que sea más estético
                shape = RoundedCornerShape(24.dp), // Bordes más redondeados tipo píldora
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("➕ Agregar Reporte", style = MaterialTheme.typography.bodyMedium)
            }

            // Botón: Ver Reportes (Más compacto)
            Button(
                onClick = onVerReportesClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("📋 Ver Reportes", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}