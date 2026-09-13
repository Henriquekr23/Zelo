package com.example.zelo.model

data class Agendamento(
    val id: Int,
    val petId: Int,
    val nomePet: String,
    val data: String,
    val horario: String,
    val descricao: String,
    val status: StatusAgendamento = StatusAgendamento.AGENDADO
) {
    fun formatarResumo(): String {
        return "$horario - $nomePet: $descricao"
    }
}