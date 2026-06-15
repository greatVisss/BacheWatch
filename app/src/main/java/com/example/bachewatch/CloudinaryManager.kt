package com.example.bachewatch

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryManager {
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return

        val config = hashMapOf(
            "cloud_name" to "dkpck4pp2"
            // Nota para tu compañero: Si Cloudinary les rechaza la subida por "falta de permisos",
            // van a necesitar agregar aquí su "api_key" y "api_secret",
            // o usar un "upload_preset" de subida no firmada (unsigned).
        )

        MediaManager.init(context, config)
        initialized = true
    }

    // --- ESTA ES LA FUNCIÓN QUE FALTABA ---
    // --- ESTA ES LA FUNCIÓN ACTUALIZADA ---
    fun subirImagen(uri: Uri, onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
        MediaManager.get().upload(uri)
            .unsigned("bachewatch-preset") // <-- ¡AQUÍ VA EL PASE DE INVITADO!
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val urlSegura = resultData["secure_url"] as String
                    onSuccess(urlSegura)
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    onFailure(error.description)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }
}