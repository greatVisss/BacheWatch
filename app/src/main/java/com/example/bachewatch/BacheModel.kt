package com.example.bachewatch

import com.google.android.gms.maps.model.LatLng


//AGREGAR DATOS FALTANTES COMO IMAGENES
//CAMBIAR VISTA AL DAR CLICK
data class Bache(
    val id: String,
    val title: String,
    val description: String,
    val position: LatLng
)