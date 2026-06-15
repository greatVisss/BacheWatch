package com.example.bachewatch

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory // <-- Requerido para cambiar el color del pin
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun InicioScreen(
    onAgregarReporteClick: () -> Unit,
    onVerReportesClick: () -> Unit
) {
    var baches by remember { mutableStateOf<List<Bache>>(emptyList()) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    LaunchedEffect(Unit) {
        FirestoreManager.obtenerBaches(
            onSuccess = { baches = it
                it.forEach { bache ->
                            println(
                                "BACHE: ${bache.title} (${bache.latitude}, ${bache.longitude})"
                                )
                            }
                        },
            onFailure = { it.printStackTrace() }
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(19.432608, -99.133209),
            17f
        )
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if(location != null){
                    val latLng = LatLng(
                        location.latitude,
                        location.longitude
                    )
                    userLocation = latLng

                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(
                            latLng,
                            17f
                        )
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. EL MAPA OCUPA TODA LA PANTALLA ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission)
        ) {
            baches.forEach { bache ->
                // --- MARCADORES INTELIGENTES ---
                // Evaluamos el texto de dangerLevel para asignar el color correspondiente
                val colorMarcador = when (bache.dangerLevel.lowercase().trim()) {
                    "bajo" -> BitmapDescriptorFactory.HUE_GREEN   // Verde para peligro bajo
                    "medio" -> BitmapDescriptorFactory.HUE_ORANGE // Naranja para peligro medio
                    "alto" -> BitmapDescriptorFactory.HUE_RED     // Rojo para peligro alto
                    else -> BitmapDescriptorFactory.HUE_RED       // Color por defecto en caso de variaciones
                }

                Marker(
                    state = MarkerState(
                        position = LatLng(bache.latitude, bache.longitude)
                    ),
                    title = bache.title,
                    snippet = "⚠️ Peligro: ${bache.dangerLevel} | 👤: ${bache.reporterName}",
                    icon = BitmapDescriptorFactory.defaultMarker(colorMarcador) // Asigna el color dinámico
                )
            }
        }

        // --- 2. TEXTOS FLOTANDO EN LA PARTE SUPERIOR ---
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 48.dp),
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
                    text = "Reporte de baches viales",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- 3. BOTONES COMPACTOS FLOTANDO EN EL LADO INFERIOR IZQUIERDO ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .width(260.dp)
                .padding(start = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onAgregarReporteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(25.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("➕ Agregar Reporte", style = MaterialTheme.typography.bodyMedium)
            }

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