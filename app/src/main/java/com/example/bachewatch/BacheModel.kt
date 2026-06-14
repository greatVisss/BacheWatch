package com.example.bachewatch

import com.google.android.gms.maps.model.LatLng

data class Bache(
    val id: String = "",
    val reporterName: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,

    val size: String = "",
    val dangerLevel: String = "",

    val title: String = "",
    val description: String = "",

    val imageUrl: String = "",

    val createdAt: Long = 0L,

    val isRepaired: Boolean = false
)