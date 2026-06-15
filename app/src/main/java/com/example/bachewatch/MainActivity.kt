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
                var pantallaActual by remember { mutableStateOf("login") }

                // NUEVA VARIABLE: Guarda el nombre del usuario activo en esta sesión
                var usuarioLogueado by remember { mutableStateOf("") }

                when (pantallaActual) {
                    "login" -> {
                        LoginScreen(
                            onLoginSuccess = { nombre ->
                                usuarioLogueado = nombre // <-- Guardamos el nombre que viene del Login
                                pantallaActual = "inicio"
                            }
                        )
                    }
                    "inicio" -> {
                        InicioScreen(
                            onAgregarReporteClick = { pantallaActual = "formulario" },
                            onVerReportesClick = { pantallaActual = "historial" }
                        )
                    }
                    "formulario" -> {
                        // MODIFICADO: Le pasamos el usuario logueado a la actividad del formulario
                        FormularioBacheScreen(
                            onAtrasClick = { pantallaActual = "inicio" },
                            onReporteGuardado = { pantallaActual = "inicio" }
                        )
                    }
                    "historial" -> {
                        // El historial ya lee de Firebase de forma independiente
                        ListaReportesScreen(
                            onAtrasClick = { pantallaActual = "inicio" }
                        )
                    }
                }
            }
        }
    }
}