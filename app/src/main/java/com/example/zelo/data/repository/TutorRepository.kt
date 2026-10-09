package com.example.zelo.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TutorRepository(context: Context) {

    private val preferences = context
        .applicationContext
        .getSharedPreferences(
            "zelo_tutor",
            Context.MODE_PRIVATE
        )

    private val _nome = MutableStateFlow(
        preferences.getString(
            "nome_tutor",
            "Desconhecido"
        ) ?: "Desconhecido"
    )

    val nome: StateFlow<String> = _nome

    fun atualizarNome(novoNome: String) {

        val nomeValidado = novoNome.trim()

        require(nomeValidado.isNotBlank()) {
            "O nome não pode estar vazio."
        }

        val salvo = preferences.edit()
            .putString("nome_tutor", nomeValidado)
            .commit()

        check(salvo) {
            "Não foi possível salvar o nome."
        }

        _nome.value = nomeValidado
    }
}