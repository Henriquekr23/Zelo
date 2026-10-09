package com.example.zelo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.zelo.data.repository.TutorRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TutorViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        TutorRepository(application)

    private val _nome = MutableStateFlow(
        repository.nome.value
    )

    val nome: StateFlow<String> = _nome

    fun atualizarNome(novoNome: String): Boolean {

        return try {
            repository.atualizarNome(novoNome)
            _nome.value = repository.nome.value
            true
        } catch (e: Exception) {
            false
        }
    }
}