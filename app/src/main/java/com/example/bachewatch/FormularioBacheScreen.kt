package com.example.bachewatch

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioBacheScreen(
    onAtrasClick: () -> Unit,
    onReporteGuardado: () -> Unit
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var reporterName by remember { mutableStateOf(SessionManager.usuarioActivo) }

    // Estados de datos
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Ahora Tamaño y Peligro se seleccionan mediante botones (Chips)
    var size by remember { mutableStateOf("Mediano") }
    var dangerLevel by remember { mutableStateOf("Medio") }

    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var addressName by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val scrollState = rememberScrollState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                    } else {
                        Toast.makeText(context, "Asegúrate de encender el GPS", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                Toast.makeText(context, "Se requiere la ubicación", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> imageUri = uri }
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
                    addressName = "Coordenadas: $latitude, $longitude"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface) // Fondo limpio
    ) {
        // --- BARRA SUPERIOR ---
        TopAppBar(
            title = { Text("Nuevo Reporte", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onAtrasClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // --- CONTENIDO DESLIZABLE ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1. ZONA DE FOTOGRAFÍA INTERACTIVA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    )
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Evidencia",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Etiqueta flotante para avisar que se puede cambiar
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    ) {
                        Text(
                            "Tocar para cambiar",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.CameraAlt,
                            "Cámara",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Toca para añadir la foto del bache",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. TARJETA DE UBICACIÓN
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                        alpha = 0.4f
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        "GPS",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Ubicación actual",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = addressName
                                ?: if (latitude != null) "⏳ Traduciendo..." else "Ubicación requerida",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    if (location != null) {
                                        latitude = location.latitude
                                        longitude = location.longitude
                                    }
                                }
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (latitude == null) "Detectar" else "Actualizar")
                    }
                }
            }

            // 3. DATOS GENERALES
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Información del bache",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { /* No se requiere lógica aquí porque es de solo lectura */ },
                    label = { Text("Tu Nombre (Cuenta Activa)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    readOnly = true, // <-- NUEVO: Bloquea el teclado para que no se pueda alterar
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre breve del bache") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción del bache") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )
            }

            // 4. CHIPS DE TAMAÑO
            Column {
                Text(
                    "Tamaño del bache",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Pequeño", "Mediano", "Grande").forEach { opcion ->
                        FilterChip(
                            selected = size == opcion,
                            onClick = { size = opcion },
                            label = { Text(opcion) }
                        )
                    }
                }
            }

            // 5. CHIPS DE NIVEL DE PELIGRO (Con colores)
            Column {
                Text(
                    "Nivel de peligro",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val opcionesPeligro = listOf("Bajo", "Medio", "Alto")
                    opcionesPeligro.forEach { opcion ->
                        val colorChip = when (opcion) {
                            "Bajo" -> Color(0xFF4CAF50) // Verde
                            "Medio" -> Color(0xFFFF9800) // Naranja
                            else -> Color(0xFFF44336) // Rojo
                        }

                        FilterChip(
                            selected = dangerLevel == opcion,
                            onClick = { dangerLevel = opcion },
                            label = { Text(opcion) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorChip.copy(alpha = 0.2f),
                                selectedLabelColor = colorChip
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = dangerLevel == opcion,
                                borderColor = colorChip
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. BOTÓN FLOTANTE PARA SUBIR
            Button(
                onClick = {
                    if (title.isNotEmpty() && reporterName.isNotEmpty() && latitude != null && imageUri != null) {

                        Toast.makeText(
                            context,
                            "Subiendo evidencia fotográfica...",
                            Toast.LENGTH_SHORT
                        ).show()

                        // --- 1. PRIMERO SUBIMOS LA FOTO A CLOUDINARY ---
                        // Nota: Asegúrate de usar el nombre exacto de la función que tu compañero
                        // haya creado en CloudinaryManager para subir archivos.
                        CloudinaryManager.subirImagen(
                            uri = imageUri!!,
                            onSuccess = { urlCloudinary ->

                                // --- 2. CLOUDINARY RESPONDIÓ, AHORA GUARDAMOS EN FIREBASE ---
                                Toast.makeText(
                                    context,
                                    "Guardando reporte...",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val nuevoBache = Bache(
                                    id = java.util.UUID.randomUUID().toString(),
                                    reporterName = reporterName,
                                    latitude = latitude!!,
                                    longitude = longitude!!,
                                    size = size,
                                    dangerLevel = dangerLevel,
                                    title = title,
                                    description = description,
                                    imageUrl = urlCloudinary, // <-- ¡AQUÍ ESTÁ LA MAGIA! Insertamos el Link Real
                                    createdAt = System.currentTimeMillis(),
                                    isRepaired = false,
                                    addressName = addressName ?: ""
                                )

                                FirestoreManager.guardarBache(
                                    bache = nuevoBache,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "¡Reporte guardado!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onReporteGuardado()
                                    },
                                    onFailure = { error ->
                                        Toast.makeText(
                                            context,
                                            "Error en Firebase: ${error.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            },
                            onFailure = { error ->
                                Toast.makeText(
                                    context,
                                    "Error al subir la foto",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Faltan datos (nombre del bache, foto o ubicación)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                Text("🚀 Subir Reporte", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}