package com.example.bachewatch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import android.widget.Toast
import coil.compose.AsyncImage

@Composable
fun ListaReportesScreen(
    onAtrasClick: () -> Unit
) {
    val context = LocalContext.current
    var baches by remember { mutableStateOf<List<Bache>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirestoreManager.obtenerBaches(
            onSuccess = { listaBaches ->
                baches = listaBaches
                cargando = false
            },
            onFailure = { error ->
                error.printStackTrace()
                cargando = false
                Toast.makeText(context, "Error al cargar reportes de Firebase", Toast.LENGTH_SHORT).show()
            }
        )
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
            Text("Historial de Reportes", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (baches.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se han encontrado reportes activos.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(baches) { bache ->
                    // Modificado: Ya no pedimos la acción de descarga
                    BacheItemCard(bache = bache)
                }
            }
        }
    }
}

@Composable
fun BacheItemCard(
    bache: Bache
) {
    // 1. Extraemos y evaluamos los datos de forma segura fuera de la interfaz
    val nombreCiudadano = if (bache.reporterName.isNotEmpty()) bache.reporterName else "Ciudadano Anónimo"
    val peligro = if (bache.dangerLevel.isNotEmpty()) bache.dangerLevel else "N/A"
    val tamano = if (bache.size.isNotEmpty()) bache.size else "N/A"

    // Nota: Asegúrate de que 'addressName' esté declarada en tu BacheModel.kt
    val ubicacion = if (bache.addressName.isNotEmpty()) bache.addressName else "Dirección no registrada (${bache.latitude}, ${bache.longitude})"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = bache.imageUrl,
                contentDescription = "Evidencia del bache",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = bache.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // 2. Ahora usamos nuestras variables locales súper limpias
                Text(
                    text = "👤 Reportado por: $nombreCiudadano",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )

                if (!bache.description.isNullOrEmpty()) {
                    Text(
                        text = bache.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⚠️ Peligro: $peligro",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "📐 Tamaño: $tamano",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "📍 $ubicacion",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}