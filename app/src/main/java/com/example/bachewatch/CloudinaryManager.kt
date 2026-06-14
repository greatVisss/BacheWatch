package com.example.bachewatch

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryManager {

    private var initialized = false

    fun init(context: Context) {

        if(initialized) return

        val config = hashMapOf(
            "cloud_name" to "dkpck4pp2"
        )

        MediaManager.init(context, config)

        initialized = true
    }
}