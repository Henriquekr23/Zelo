package com.example.zelo.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

import com.example.zelo.data.local.ZeloDatabase
import com.example.zelo.data.repository.AgendamentoRepository
import com.example.zelo.data.repository.PetRepository
import com.example.zelo.model.Agendamento
import com.example.zelo.model.Pet
import com.example.zelo.model.StatusAgendamento

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HistoricoUiState(
    val pets: List<Pet> = emptyList(),
    val petAtivo: Pet? = null,
    val historico: List<Agendamento> = emptyList(),
    val carregando: Boolean = true
)

class HistoricoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        ZeloDatabase.getInstance(application)

    private val petRepository = PetRepository(
        database.petDao()
    )

    private val agendamentoRepository =
        AgendamentoRepository(
            database.agendamentoDao()
        )

    private var petSelecionadoId: Int? = null
    private var agendamentosAtuais: List<Agendamento> =
        emptyList()

    private val _uiState = mutableStateOf(
        HistoricoUiState()
    )

    val uiState: State<HistoricoUiState>
        get() = _uiState

    init {
        observarDados()
    }

    private fun observarDados() {

        viewModelScope.launch {

            combine(
                petRepository.observarPorTutor(1),
                agendamentoRepository.todos
            ) { pets, agendamentos ->

                Pair(pets, agendamentos)

            }.collect { (pets, agendamentos) ->

                agendamentosAtuais = agendamentos

                val petAtivo = pets.firstOrNull {
                    it.id == petSelecionadoId
                } ?: pets.firstOrNull()

                petSelecionadoId = petAtivo?.id

                val historico = agendamentos
                    .filter {
                        it.petId == petAtivo?.id &&
                                it.status == StatusAgendamento.CONCLUIDO
                    }
                    .sortedWith(
                        compareByDescending<Agendamento> {
                            chaveData(it.data)
                        }.thenByDescending {
                            it.horario
                        }
                    )

                _uiState.value = HistoricoUiState(
                    pets = pets,
                    petAtivo = petAtivo,
                    historico = historico,
                    carregando = false
                )
            }
        }
    }

    fun selecionarPet(petId: Int) {

        val estadoAtual = _uiState.value

        val pet = estadoAtual.pets.firstOrNull {
            it.id == petId
        } ?: return

        petSelecionadoId = petId

        val historico = agendamentosAtuais
            .filter {
                it.petId == petId &&
                        it.status == StatusAgendamento.CONCLUIDO
            }
            .sortedWith(
                compareByDescending<Agendamento> {
                    chaveData(it.data)
                }.thenByDescending {
                    it.horario
                }
            )

        _uiState.value = estadoAtual.copy(
            petAtivo = pet,
            historico = historico
        )
    }

    private fun chaveData(data: String): String {

        val partes = data.split("/")

        if (partes.size != 3) return ""

        return "${partes[2]}-${partes[1]}-${partes[0]}"
    }
}