package com.example.bachewatch

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun FormularioBacheScreen(
    onAtrasClick: () -> Unit,
    onReporteGuardado: () -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // NUEVO CAMPO: Nombre de quien reporta
    var reporterName by remember { mutableStateOf("") }

    var description by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var dangerLevel by remember { mutableStateOf("") }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var addressName by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                        } else {
                            Toast.makeText(context, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
        }
    )

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude!!, longitude!!, 1)
                    if (!addresses.isNullOrEmpty()) {
                        addressName = addresses[0].getAddressLine(0)
                    } else {
                        addressName = "Dirección no encontrada"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    addressName = "Coordenadas: $latitude, $longitude"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAtrasClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar al inicio"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reportar un Bache", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NUEVO TEXTFIELD: Para el nombre del ciudadano
        OutlinedTextField(
            value = reporterName,
            onValueChange = { reporterName = it },
            label = { Text("Tu Nombre (Ciudadano que reporta)") },
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

        Button(
            onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            latitude = location.latitude
                            longitude = location.longitude
                        } else {
                            Toast.makeText(context, "Asegúrate de tener el GPS encendido", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("📍 Obtener mi ubicación actual")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (addressName != null) {
            Text(text = "📍 $addressName", color = MaterialTheme.colorScheme.primary)
        } else if (latitude != null && longitude != null) {
            Text(text = "⏳ Traduciendo coordenadas...", color = MaterialTheme.colorScheme.primary)
        } else {
            Text(
                text = "* La ubicación es obligatoria para el reporte",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(if (imageUri != null) "📸 Cambiar Fotografía" else "📸 Añadir Evidencia Fotográfica")
        }

        if (imageUri != null) {
            Spacer(modifier = Modifier.height(16.dp))
            AsyncImage(
                model = imageUri,
                contentDescription = "Vista previa del bache",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                // Validación actualizada para incluir el nombre del reportero
                if (description.isNotEmpty() && size.isNotEmpty() && dangerLevel.isNotEmpty() && reporterName.isNotEmpty() && latitude != null && imageUri != null) {
                    Toast.makeText(context, "Listo para Firebase! Reportado por: $reporterName en $addressName", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Faltan datos (nombre, título, foto o ubicación)", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Subir Reporte")
        }
    }
}