package com.example.zelo.model

data class Pet(
    val id: Int,
    val nome: String,
    val especie: String,
    val raca: String,
    val tutorId: Int,
    val anoNascimento: Int = 2022,
    val emoji: String = "🐾"
)
