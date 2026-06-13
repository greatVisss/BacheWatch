package com.example.bachewatch

import com.example.bachewatch.Bache
import com.google.android.gms.maps.model.LatLng


//LLENAR CON BASE DE DATOS (FIREBASE)
object BachesRepository {

    fun getBaches(): List<Bache> {
        return listOf(
            Bache(
                id = "1",
                title = "Bache 1",
                description = "Avenida principal",
                position = LatLng(19.432608, -99.133209)
            ),
            Bache(
                id = "2",
                title = "Bache 2",
                description = "Cruce peligroso",
                position = LatLng(19.435000, -99.140000)
            ),
            Bache(
                id = "3",
                title = "Bache 3",
                description = "Zona escolar",
                position = LatLng(19.428000, -99.129000)
            )
        )
    }
}
