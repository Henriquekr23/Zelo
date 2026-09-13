package com.example.zelo.model

class AgendaModel {

    private val agendamentos = mutableListOf(
        Agendamento(
            id = 1,
            petId = 1,
            nomePet = "Akira",
            data = "10/09/2026",
            horario = "14:00",
            descricao = "Consulta de retorno"
        ),
        Agendamento(
            id = 2,
            petId = 1,
            nomePet = "Akira",
            data = "15/09/2026",
            horario = "10:30",
            descricao = "Vacina antirrábica"
        )
    )

    fun listarPorData(data: String): List<Agendamento> {
        return agendamentos.filter { agendamento ->
            agendamento.data == data
        }
    }

    fun adicionar(agendamento: Agendamento): List<Agendamento> {
        agendamentos.add(agendamento)
        return listarPorData(agendamento.data)
    }
}