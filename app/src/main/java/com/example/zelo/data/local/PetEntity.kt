package com.example.zelo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class PetEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String,

    val especie: String,

    val raca: String,

    val tutorId: Int,

    val anoNascimento: Int = 2022,

    val emoji: String = "🐾"
)