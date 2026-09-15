package com.example.zelo.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.zelo.model.LembretesModel
import com.example.zelo.model.Pet
import com.example.zelo.model.Procedimento

data class LembretesUiState(
    val pet: Pet,
    val vencimentos: List<Procedimento>
)

class LembretesViewModel : ViewModel() {

    private val model = LembretesModel()

    private val _uiState = mutableStateOf(
        carregarEstadoInicial()
    )

    val uiState: State<LembretesUiState>
        get() = _uiState

    private fun carregarEstadoInicial(): LembretesUiState {
        val pet = model.buscarPetAtivo()

        return LembretesUiState(
            pet = pet,
            vencimentos = model.buscarProximosVencimentos(
                petId = pet.id
            )
        )
    }
}