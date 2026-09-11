package com.example.zelo.model

data class Procedimento(
    val id: Int,
    val petId: Int,
    val tipo: TipoProcedimento,
    val descricao: String,
    val data: String
)