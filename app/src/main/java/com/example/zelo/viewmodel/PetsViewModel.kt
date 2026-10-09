package com.example.zelo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zelo.data.local.ZeloDatabase
import com.example.zelo.data.repository.PetRepository
import com.example.zelo.model.Pet
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.zelo.data.local.AgendamentoDao
import com.example.zelo.data.repository.AgendamentoRepository
import com.example.zelo.model.Agendamento
import kotlinx.coroutines.flow.first
import com.example.zelo.model.StatusAgendamento

class PetsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val agendamentoDao: AgendamentoDao =
        ZeloDatabase
            .getInstance(application)
            .agendamentoDao()

    private val repository = PetRepository(
        ZeloDatabase
            .getInstance(application)
            .petDao()
    )

    private val _pets = MutableLiveData<List<Pet>>(emptyList())

    val pets: LiveData<List<Pet>> = _pets

    init {
        carregarPets()
    }

    private fun carregarPets() {

        viewModelScope.launch {

            repository.observarPorTutor(tutorId = 1)
                .collect { listaPets ->

                    _pets.value = listaPets

                }
        }
    }

    fun cadastrarPet(
        nome: String,
        especie: String,
        raca: String,
        anoNascimento: Int,
        emoji: String
    ) {

        viewModelScope.launch {

            repository.cadastrar(
                nome = nome,
                especie = especie,
                raca = raca,
                tutorId = 1,
                anoNascimento = anoNascimento,
                emoji = emoji
            )
        }
    }

    fun atualizarPet(
        id: Int,
        nome: String,
        especie: String,
        raca: String,
        anoNascimento: Int,
        emoji: String
    ) {
        viewModelScope.launch {

            val petExistente = repository.buscarPorId(id)

            if (petExistente != null) {

                repository.atualizar(
                    petExistente.copy(
                        nome = nome.trim(),
                        especie = especie.trim(),
                        raca = raca.trim(),
                        anoNascimento = anoNascimento,
                        emoji = emoji
                    )
                )
            }
        }
    }

    fun excluirPet(
        id: Int,
        onResultado: (Boolean) -> Unit
    ) {
        viewModelScope.launch {

            val quantidadeAgendamentos =
                agendamentoDao.contarPorPet(id)

            if (quantidadeAgendamentos > 0) {

                onResultado(false)

                return@launch
            }

            val linhasExcluidas = repository.excluir(id)

            onResultado(linhasExcluidas > 0)
        }
    }

    fun calcularIdade(anoNascimento: Int): Int {

        val anoAtual = Calendar.getInstance()
            .get(Calendar.YEAR)

        return (anoAtual - anoNascimento)
            .coerceAtLeast(0)
    }

    private val agendamentoRepository = AgendamentoRepository(
        ZeloDatabase
            .getInstance(application)
            .agendamentoDao()
    )

    fun buscarAgendamentosDoPet(
        petId: Int,
        onResultado: (List<Agendamento>) -> Unit
    ) {
        viewModelScope.launch {

            val agendamentos = agendamentoRepository
                .observarPorPet(petId)
                .first()

            onResultado(agendamentos)
        }
    }

    fun alterarStatusAgendamento(
        agendamentoId: Int,
        petId: Int,
        novoStatus: StatusAgendamento,
        onResultado: (Boolean) -> Unit
    ) {

        viewModelScope.launch {

            val atualizado = try {
                agendamentoRepository.atualizarStatus(
                    agendamentoId = agendamentoId,
                    petId = petId,
                    novoStatus = novoStatus
                )
            } catch (e: Exception) {
                false
            }

            onResultado(atualizado)
        }
    }
}