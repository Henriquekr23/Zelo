package com.example.zelo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.zelo.model.Agendamento
import com.example.zelo.model.StatusAgendamento

@Entity(tableName = "agendamentos")
data class AgendamentoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val petId: Int,

    val nomePet: String,

    val data: String,

    val horario: String,

    val descricao: String,

    val status: String = StatusAgendamento.AGENDADO.name

) {

    fun toDomain(): Agendamento {
        return Agendamento(
            id = id,
            petId = petId,
            nomePet = nomePet,
            data = data,
            horario = horario,
            descricao = descricao,
            status = StatusAgendamento.valueOf(status)
        )
    }

    companion object {

        fun fromDomain(
            agendamento: Agendamento
        ): AgendamentoEntity {

            return AgendamentoEntity(
                id = agendamento.id,
                petId = agendamento.petId,
                nomePet = agendamento.nomePet,
                data = agendamento.data,
                horario = agendamento.horario,
                descricao = agendamento.descricao,
                status = agendamento.status.name
            )
        }
    }
}