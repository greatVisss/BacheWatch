package com.example.bachewatch

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaReportesScreen(
    onAtrasClick: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    // Estados para manejar los datos y la pantalla de carga
    var listaDeBaches by remember { mutableStateOf<List<Bache>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    // Descargamos los datos desde Firebase apenas se abre la pantalla
    LaunchedEffect(Unit) {
        db.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING) // Los más recientes primero
            .get()
            .addOnSuccessListener { result ->
                val bachesDescargados = mutableListOf<Bache>()
                for (document in result) {
                    try {
                        val bache = Bache(
                            id = document.getString("id") ?: "",
                            reporterName = document.getString("reporterName") ?: "",
                            latitude = document.getDouble("latitude") ?: 0.0,
                            longitude = document.getDouble("longitude") ?: 0.0,
                            size = document.getString("size") ?: "",
                            dangerLevel = document.getString("dangerLevel") ?: "",
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            createdAt = document.getLong("createdAt") ?: 0L,
                            isRepaired = document.getBoolean("isRepaired") ?: false,
                            addressName = document.getString("addressName") ?: ""
                        )
                        bachesDescargados.add(bache)
                    } catch (e: Exception) {
                        e.printStackTrace() // Evita que la app se caiga si un registro está mal escrito
                    }
                }
                listaDeBaches = bachesDescargados
                cargando = false
            }
            .addOnFailureListener {
                cargando = false
                Toast.makeText(context, "Error al cargar el historial de Firebase", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Reportes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onAtrasClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) { paddingValues ->

        if (cargando) {
            // Pantalla de carga mientras responde Firebase
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (listaDeBaches.isEmpty()) {
            // Pantalla si la base de datos está vacía
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Aún no hay baches reportados.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Lista real pintando las tarjetas
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(listaDeBaches) { bache ->
                    BacheItemCard(bache = bache)
                }
            }
        }
    }
}

@Composable
fun BacheItemCard(bache: Bache) {
    // 1. Validaciones
    val nombreCiudadano = if (bache.reporterName.isNotEmpty()) bache.reporterName else "Anónimo"
    val ubicacion = if (bache.addressName.isNotEmpty()) bache.addressName else "Coordenadas: ${bache.latitude}, ${bache.longitude}"

    // 2. Formateo de Fecha
    val fechaFormateada = remember(bache.createdAt) {
        if (bache.createdAt > 0L) {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy • HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(bache.createdAt))
        } else {
            "Fecha desconocida"
        }
    }

    // 3. Lógica de colores para la etiqueta de peligro
    val colorPeligro = when (bache.dangerLevel.lowercase()) {
        "bajo" -> Color(0xFF4CAF50) // Verde
        "medio" -> Color(0xFFFF9800) // Naranja
        else -> Color(0xFFF44336) // Rojo
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // --- HEADER: IMAGEN ---
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                AsyncImage(
                    model = bache.imageUrl,
                    contentDescription = "Evidencia del bache",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                ) {
                    Text(
                        text = "📏 ${bache.size}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // --- CUERPO: INFO PRINCIPAL ---
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorPeligro.copy(alpha = 0.15f),
                    contentColor = colorPeligro
                ) {
                    Text(
                        text = "Riesgo ${bache.dangerLevel}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = bache.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!bache.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = bache.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = ubicacion,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

            // --- PIE DE PÁGINA: USUARIO Y FECHA ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = nombreCiudadano,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = fechaFormateada,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}