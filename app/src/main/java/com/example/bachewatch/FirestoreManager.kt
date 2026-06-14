package com.example.bachewatch

import com.google.firebase.firestore.FirebaseFirestore

object FirestoreManager {

    private val db = FirebaseFirestore.getInstance()

    fun guardarBache(
        bache: Bache,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reports")
            .document(bache.id)
            .set(bache)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun obtenerBaches(
        onSuccess: (List<Bache>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reports")
            //.whereEqualTo("isRepaired", false)
            .get()
            .addOnSuccessListener { result ->

                val lista = result.documents.mapNotNull {
                    it.toObject(Bache::class.java)
                }

                onSuccess(lista)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}