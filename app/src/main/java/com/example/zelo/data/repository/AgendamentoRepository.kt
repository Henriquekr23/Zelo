package com.example.zelo.data.repository

import com.example.zelo.data.local.AgendamentoDao
import com.example.zelo.data.local.AgendamentoEntity
import com.example.zelo.model.Agendamento
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.zelo.model.StatusAgendamento

class AgendamentoRepository(
    private val dao: AgendamentoDao
) {

    val todos: Flow<List<Agendamento>> =
        dao.observarTodos().map { registros ->

            registros.map { entidade ->
                entidade.toDomain()
            }
        }

    suspend fun adicionar(
        agendamento: Agendamento
    ): Long {

        val entidade = AgendamentoEntity.fromDomain(
            agendamento.copy(id = 0)
        )

        return dao.inserir(entidade)
    }

    suspend fun excluir(id: Int) {
        dao.excluir(id)
    }

    suspend fun atualizar(
        agendamento: Agendamento
    ) {

        dao.atualizar(
            AgendamentoEntity.fromDomain(agendamento)
        )
    }

    fun observarPorPet(
        petId: Int
    ): Flow<List<Agendamento>> {

        return dao.observarPorPet(petId).map { entidades ->

            entidades.map { entidade ->
                entidade.toDomain()
            }
        }
    }

    suspend fun atualizarStatus(
        agendamentoId: Int,
        petId: Int,
        novoStatus: StatusAgendamento
    ): Boolean {

        require(
            novoStatus == StatusAgendamento.CONCLUIDO ||
                    novoStatus == StatusAgendamento.CANCELADO
        ) {
            "Transição de status inválida"
        }

        val linhasAtualizadas = dao.atualizarStatus(
            agendamentoId = agendamentoId,
            petId = petId,
            novoStatus = novoStatus.name
        )

        return linhasAtualizadas > 0
    }
}