package com.example.bachewatch

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices

@Composable
fun FormularioBacheScreen(onReporteGuardado: () -> Unit) {
    val context = LocalContext.current
    // Cliente para obtener la ubicación GPS
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Variables de estado para los campos de texto
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var dangerLevel by remember { mutableStateOf("") }

    // Variables para almacenar la ubicación y la foto
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    // Permiso de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                        }
                    }
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(context, "Se requiere la ubicación para reportar", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // Selector de imágenes de la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    // Efecto que se ejecuta al abrir la pantalla para checar/pedir ubicación
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                }
            }
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Reportar un Bache", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título del reporte") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = size,
            onValueChange = { size = it },
            label = { Text("Tamaño (ej. Pequeño, Mediano)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = dangerLevel,
            onValueChange = { dangerLevel = it },
            label = { Text("Nivel de Peligro (Bajo, Medio, Alto)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Indicador de ubicación
        Text(
            text = if (latitude != null && longitude != null)
                "📍 Ubicación obtenida automáticamente"
            else "⏳ Obteniendo ubicación GPS...",
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Botón para adjuntar foto
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(if (imageUri != null) "📸 Foto adjuntada (Cambiar)" else "📸 Añadir Evidencia Fotográfica")
        }

        Spacer(modifier = Modifier.weight(1f)) // Empuja el botón final hacia abajo

        // Botón principal de envío
        Button(
            onClick = {
                if (title.isNotEmpty() && latitude != null && imageUri != null) {
                    // TODO: Aquí entrará la lógica de Firebase Storage y Firestore
                    Toast.makeText(context, "Listo para conectar con Firebase", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Faltan datos obligatorios, foto o ubicación", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Subir Reporte")
        }
    }
}