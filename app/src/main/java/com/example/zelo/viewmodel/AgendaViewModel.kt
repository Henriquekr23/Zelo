package com.example.zelo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zelo.model.AgendaModel
import com.example.zelo.model.Agendamento

class AgendaViewModel : ViewModel() {

    private val model = AgendaModel()

    private val _agendamentos =
        MutableLiveData<List<Agendamento>>()

    val agendamentos: LiveData<List<Agendamento>>
        get() = _agendamentos

    private val _dataSelecionada =
        MutableLiveData("10/09/2026")

    val dataSelecionada: LiveData<String>
        get() = _dataSelecionada

    init {
        carregarAgendamentos("10/09/2026")
    }

    fun carregarAgendamentos(data: String) {
        _dataSelecionada.value = data
        _agendamentos.value = model.listarPorData(data)
    }

    fun adicionarAgendamento(
        agendamento: Agendamento
    ) {
        model.adicionar(agendamento)

        val dataAtual =
            _dataSelecionada.value ?: agendamento.data

        _agendamentos.value =
            model.listarPorData(dataAtual)
    }
}