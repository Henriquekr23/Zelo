package com.example.zelo.data.repository

import com.example.zelo.data.local.PetDao
import com.example.zelo.data.local.PetEntity
import com.example.zelo.model.Pet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PetRepository(
    private val dao: PetDao
) {

    fun observarPorTutor(tutorId: Int): Flow<List<Pet>> {
        return dao.observarPorTutor(tutorId).map { entidades ->

            entidades.map { entidade ->
                Pet(
                    id = entidade.id,
                    nome = entidade.nome,
                    especie = entidade.especie,
                    raca = entidade.raca,
                    tutorId = entidade.tutorId,
                    anoNascimento = entidade.anoNascimento,
                    emoji = entidade.emoji
                )
            }
        }
    }

    suspend fun buscarPorId(id: Int): PetEntity? {
        return dao.buscarPorId(id)
    }

    suspend fun cadastrar(
        nome: String,
        especie: String,
        raca: String,
        tutorId: Int,
        anoNascimento: Int,
        emoji: String
    ): Long {

        require(nome.isNotBlank()) {
            "O nome do pet é obrigatório."
        }

        require(especie.isNotBlank()) {
            "A espécie do pet é obrigatória."
        }

        val anoAtual = java.util.Calendar.getInstance()
            .get(java.util.Calendar.YEAR)

        require(anoNascimento in 1900..anoAtual) {
            "O ano de nascimento é inválido."
        }

        return dao.inserir(
            PetEntity(
                nome = nome.trim(),
                especie = especie.trim(),
                raca = raca.trim(),
                tutorId = tutorId,
                anoNascimento = anoNascimento,
                emoji = emoji.ifBlank { "🐾" }
            )
        )
    }

    suspend fun atualizar(pet: PetEntity): Int {
        return dao.atualizar(pet)
    }

    suspend fun excluir(id: Int): Int {
        return dao.excluir(id)
    }
}