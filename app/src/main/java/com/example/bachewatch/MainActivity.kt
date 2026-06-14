package com.example.bachewatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.bachewatch.ui.theme.BacheWatchTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseFirestore.getInstance()
        CloudinaryManager.init(this)

        setContent {
            BacheWatchTheme {
                val context = LocalContext.current

                // Usamos un String para manejar los estados de las pantallas: "inicio", "formulario", "historial"
                var pantallaActual by remember { mutableStateOf("inicio") }

                when (pantallaActual) {
                    "inicio" -> {
                        InicioScreen(
                            onAgregarReporteClick = {
                                pantallaActual = "formulario"
                            },
                            onVerReportesClick = {
                                pantallaActual = "historial" // <-- NUEVO: Ahora cambia el estado para abrir el historial
                            }
                        )
                    }
                    "formulario" -> {
                        FormularioBacheScreen(
                            onAtrasClick = {
                                pantallaActual = "inicio"
                            },
                            onReporteGuardado = {
                                pantallaActual = "inicio"
                            }
                        )
                    }
                    "historial" -> { // <-- NUEVO: Agregamos el contenedor de la pantalla de historial
                        ListaReportesScreen(
                            onAtrasClick = {
                                pantallaActual = "inicio" // Al regresar, volvemos al mapa central
                            }
                        )
                    }
                }
            }
        }
    }
}