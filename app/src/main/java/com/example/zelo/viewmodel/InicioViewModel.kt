package com.example.zelo.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.zelo.model.InicioModel
import com.example.zelo.model.Pet
import com.example.zelo.model.Procedimento
import com.example.zelo.model.Tutor

data class InicioUiState(
    val tutor: Tutor,
    val petAtivo: Pet,
    val todosPets: List<Pet>,
    val proximoLembrete: Procedimento?,
    val proximosCuidados: List<Procedimento>
)

class InicioViewModel : ViewModel() {

    private val model = InicioModel()

    private val _uiState = mutableStateOf(carregarEstadoInicial())

    val uiState: State<InicioUiState>
        get() = _uiState

    private fun carregarEstadoInicial(): InicioUiState {
        val pet = model.buscarPetAtivo()

        return InicioUiState(
            tutor = model.buscarTutorAtual(),
            petAtivo = pet,
            todosPets = model.todosPets,
            proximoLembrete =
                model.buscarProximoLembrete(pet.id),
            proximosCuidados =
                model.buscarProximosCuidados(
                    petId = pet.id,
                    limite = 3
                )
        )
    }

    fun selecionarPet(pet: Pet) {
        _uiState.value = _uiState.value.copy(
            petAtivo = pet,
            proximoLembrete = model.buscarProximoLembrete(pet.id),
            proximosCuidados = model.buscarProximosCuidados(petId = pet.id, limite = 3)
        )
    }
}