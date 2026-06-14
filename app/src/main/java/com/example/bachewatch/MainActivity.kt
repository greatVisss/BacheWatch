package com.example.bachewatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bachewatch.ui.theme.BacheWatchTheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.GoogleMapComposable

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseFirestore.getInstance()
        CloudinaryManager.init(this)

        setContent {
            BacheWatchTheme {
                // Variable de estado para controlar qué pantalla se muestra
                var mostrarFormulario by remember { mutableStateOf(false) }

                if (mostrarFormulario) {
                    // Si es true, mostramos la pantalla del formulario
                    FormularioBacheScreen(
                        onReporteGuardado = {
                            // Cuando terminemos, ocultamos el formulario para volver al mapa
                            mostrarFormulario = false
                        }
                    )
                } else {
                    // Si es false, mostramos el mapa y le pasamos la acción para abrir el form
                    WorldMap(onAgregarBacheClick = { mostrarFormulario = true })
                }
            }
        }
    }
}

@Composable
fun WorldMap(onAgregarBacheClick: () -> Unit) { // <-- Agregamos el parámetro de acción
    var isMapLoaded by remember { mutableStateOf(false) }

    var baches by remember {
        mutableStateOf<List<Bache>>(emptyList())
    }

    LaunchedEffect(Unit) {
        FirestoreManager.obtenerBaches(
            onSuccess = { baches = it },
            onFailure = { it.printStackTrace() }
        )
    }

    //CAMBIAR A POSICION REAL DE USUARIO
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(19.432608, -99.133209),
            14f
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { isMapLoaded = true }
        ) {
            baches.forEach { bache ->
                Marker(
                    state = MarkerState(
                        position = LatLng(
                            bache.latitude,
                            bache.longitude
                        )
                    ),
                    title = bache.title,
                    snippet = bache.description
                )
            }
        }

        // --- BOTÓN FLOTANTE AÑADIDO AQUÍ ---
        FloatingActionButton(
            onClick = onAgregarBacheClick,
            modifier = Modifier
                .align(Alignment.BottomEnd) // Lo posiciona abajo a la derecha
                .padding(16.dp)
                .padding(bottom = 32.dp) // Un poco de espacio extra por la barra de navegación del sistema
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Reportar Bache")
        }
    }
}