package com.example.bachewatch

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun SeleccionUbicacionScreen(
    onLocationSelected: (LatLng) -> Unit,
    onCancel: () -> Unit
) {

    var ubicacionInicial by remember { mutableStateOf<LatLng?>(null) }

    val context = LocalContext.current

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        ubicacionInicial = LatLng(
                            location.latitude,
                            location.longitude
                        )
                    }
                }
        }
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(ubicacionInicial) {

        ubicacionInicial?.let {

            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(
                    it,
                    18f
                )
        }
    }

    if (ubicacionInicial == null) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        return
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        )

        // Indicador fijo en el centro
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Ubicación seleccionada",
            tint = Color.Red,
            modifier = Modifier
                .align(Alignment.Center)
                .size(50.dp)
        )

        // Panel inferior
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {

            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {

                Text(
                    text = "Mueve el mapa hasta colocar el marcador donde quieras."
                )

                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.size(12.dp)
                )

                Button(
                    onClick = {
                        onLocationSelected(
                            cameraPositionState.position.target
                        )
                    }
                ) {
                    Text("Usar ubicación")
                }

                Button(
                    onClick = onCancel
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}