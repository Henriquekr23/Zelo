package com.example.zelo.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.zelo.model.HistoricoModel
import com.example.zelo.model.Pet
import com.example.zelo.model.Procedimento

data class HistoricoUiState(
    val petAtivo: Pet,
    val historico: List<Procedimento>
)

class HistoricoViewModel : ViewModel() {

    private val model = HistoricoModel()

    private val _uiState = mutableStateOf(carregarEstadoInicial())

    val uiState: State<HistoricoUiState>
        get() = _uiState

    private fun carregarEstadoInicial(): HistoricoUiState {
        val pet = model.buscarPetAtivo()

        return HistoricoUiState(
            petAtivo = pet,
            historico = model.buscarHistorico(pet.id)
        )
    }
}
