package com.example.bachewatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                // Estado para controlar qué pantalla completa está activa
                var pantallaActual by remember { mutableStateOf("inicio") }

                when (pantallaActual) {
                    "inicio" -> {
                        InicioScreen(
                            onAgregarReporteClick = { pantallaActual = "formulario" },
                            onVerReportesClick = { pantallaActual = "historial" }
                        )
                    }
                    "formulario" -> {
                        FormularioBacheScreen(
                            onAtrasClick = { pantallaActual = "inicio" },
                            onReporteGuardado = { pantallaActual = "inicio" }
                        )
                    }
                    "historial" -> {
                        // Carga el historial limpio que creamos anteriormente
                        ListaReportesScreen(
                            onAtrasClick = { pantallaActual = "inicio" }
                        )
                    }
                }
            }
        }
    }
}