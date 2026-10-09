package com.example.zelo.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zelo.data.local.ZeloDatabase
import com.example.zelo.data.repository.PetRepository
import com.example.zelo.data.repository.AgendamentoRepository
import com.example.zelo.model.Pet
import com.example.zelo.model.Agendamento
import com.example.zelo.model.StatusAgendamento
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InicioUiState(
    val petAtivo: Pet? = null,
    val todosPets: List<Pet> = emptyList(),
    val proximoLembrete: Agendamento? = null,
    val proximosCuidados: List<Agendamento> = emptyList()
)

class InicioViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database =
        ZeloDatabase.getInstance(application)

    private val petRepository =
        PetRepository(database.petDao())

    private val agendamentoRepository =
        AgendamentoRepository(database.agendamentoDao())

    private var petSelecionadoId: Int? = null

    private var todosAgendamentos:
            List<Agendamento> = emptyList()

    private val _uiState = mutableStateOf(
        InicioUiState()
    )

    val uiState: State<InicioUiState>
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

                todosAgendamentos = agendamentos

                val petAtivo = pets.firstOrNull {
                    it.id == petSelecionadoId
                } ?: pets.firstOrNull()

                petSelecionadoId = petAtivo?.id

                atualizarEstado(
                    pets = pets,
                    petAtivo = petAtivo
                )
            }
        }
    }

    fun selecionarPet(pet: Pet) {

        petSelecionadoId = pet.id

        atualizarEstado(
            pets = _uiState.value.todosPets,
            petAtivo = pet
        )
    }

    private fun atualizarEstado(
        pets: List<Pet>,
        petAtivo: Pet?
    ) {

        val hoje = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale("pt", "BR")
        ).format(Date())

        val dataHoje = converterData(hoje)

        val proximos = todosAgendamentos
            .filter { agendamento ->

                val data = converterData(
                    agendamento.data
                )

                agendamento.petId == petAtivo?.id &&
                        agendamento.status ==
                        StatusAgendamento.AGENDADO &&
                        data != null &&
                        dataHoje != null &&
                        !data.before(dataHoje)

            }
            .sortedWith(
                compareBy<Agendamento> {
                    converterData(it.data)?.time
                        ?: Long.MAX_VALUE
                }.thenBy {
                    it.horario
                }
            )

        _uiState.value = InicioUiState(
            petAtivo = petAtivo,
            todosPets = pets,
            proximoLembrete = proximos.firstOrNull(),
            proximosCuidados = proximos.take(3)
        )
    }

    private fun converterData(
        data: String
    ): Date? {

        return try {
            SimpleDateFormat(
                "dd/MM/yyyy",
                Locale("pt", "BR")
            ).apply {
                isLenient = false
            }.parse(data)

        } catch (e: Exception) {
            null
        }
    }
}