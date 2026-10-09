package com.example.zelo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendamentoDao {

    @Query(
        "SELECT * FROM agendamentos ORDER BY data, horario"
    )
    fun observarTodos(): Flow<List<AgendamentoEntity>>

    @Query(
        "SELECT * FROM agendamentos WHERE id = :id"
    )
    suspend fun buscarPorId(
        id: Int
    ): AgendamentoEntity?

    @Insert
    suspend fun inserir(
        agendamento: AgendamentoEntity
    ): Long

    @Update
    suspend fun atualizar(
        agendamento: AgendamentoEntity
    )

    @Query(
        "DELETE FROM agendamentos WHERE id = :id"
    )
    suspend fun excluir(
        id: Int
    )

    @Query(
        "SELECT COUNT(*) FROM agendamentos WHERE petId = :petId"
    )
    suspend fun contarPorPet(petId: Int): Int

    @Query(
        "SELECT * FROM agendamentos WHERE petId = :petId ORDER BY data, horario"
    )
    fun observarPorPet(petId: Int): Flow<List<AgendamentoEntity>>

    @Query("""
    UPDATE agendamentos
    SET status = :novoStatus
    WHERE id = :agendamentoId
    AND petId = :petId
    AND status = 'AGENDADO'
""")
    suspend fun atualizarStatus(
        agendamentoId: Int,
        petId: Int,
        novoStatus: String
    ): Int
}

