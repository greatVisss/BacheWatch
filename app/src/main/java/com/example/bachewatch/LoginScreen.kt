package com.example.bachewatch

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore // Solo usamos Firestore

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var esRegistro by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "BacheWatch",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = if (esRegistro) "Crea un usuario nuevo" else "Bienvenido de vuelta",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it.replace(" ", "") },
            label = { Text("Nombre de usuario") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (esRegistro) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar contraseña") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // --- VALIDACIONES DE LA APP ---
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (username.length < 3) {
                    Toast.makeText(context, "El usuario debe tener al menos 3 letras", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (password.length < 6) {
                    Toast.makeText(context, "La contraseña requiere mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (esRegistro && password != confirmPassword) {
                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                cargando = true

                // Convertimos el usuario a minúsculas para usarlo como ID único en la base de datos
                val userId = username.lowercase()
                val docRef = db.collection("usuarios").document(userId)

                if (esRegistro) {
                    // --- LÓGICA DE REGISTRO MANUAL ---
                    docRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            cargando = false
                            Toast.makeText(context, "Ese nombre de usuario ya está ocupado", Toast.LENGTH_SHORT).show()
                        } else {
                            val userProfile = hashMapOf(
                                "username" to username,
                                "password" to password, // Se inserta tal cual
                                "createdAt" to System.currentTimeMillis()
                            )

                            docRef.set(userProfile)
                                .addOnSuccessListener {
                                    cargando = false
                                    SessionManager.usuarioActivo = username
                                    Toast.makeText(context, "¡Usuario registrado exitosamente!", Toast.LENGTH_SHORT).show()
                                    onLoginSuccess(username)
                                }
                                .addOnFailureListener { e ->
                                    cargando = false
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    }.addOnFailureListener {
                        cargando = false
                        Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // --- LÓGICA DE INICIO DE SESIÓN MANUAL ---
                    docRef.get().addOnSuccessListener { document ->
                        cargando = false
                        if (document.exists()) {
                            val dbPassword = document.getString("password")
                            if (dbPassword == password) {
                                SessionManager.usuarioActivo = username // <-- NUEVO: Guardamos el nombre aquí
                                onLoginSuccess(username)
                            } else {
                                Toast.makeText(context, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "El usuario no existe", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener {
                        cargando = false
                        Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (esRegistro) "Registrarme" else "Iniciar Sesión",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            esRegistro = !esRegistro
            confirmPassword = ""
        }) {
            Text(
                text = if (esRegistro) "¿Ya tienes cuenta? Inicia Sesión" else "¿No tienes cuenta? Regístrate",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}