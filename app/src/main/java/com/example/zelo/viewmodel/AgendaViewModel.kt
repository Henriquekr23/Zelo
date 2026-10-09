package com.example.zelo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.zelo.model.StatusAgendamento
import com.example.zelo.data.local.ZeloDatabase
import com.example.zelo.data.repository.AgendamentoRepository
import com.example.zelo.model.Agendamento
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

class AgendaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AgendamentoRepository(
        ZeloDatabase
            .getInstance(application)
            .agendamentoDao()
    )

    private var cache: List<Agendamento> = emptyList()

    private val _agendamentos =
        MutableLiveData<List<Agendamento>>(emptyList())

    val agendamentos: LiveData<List<Agendamento>> =
        _agendamentos

    private val _dataSelecionada = MutableLiveData(
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale("pt", "BR")
        ).format(Calendar.getInstance().time)
    )

    val dataSelecionada: LiveData<String> =
        _dataSelecionada

    init {

        viewModelScope.launch {

            repository.todos.collect { registros ->

                cache = registros

                atualizarLista()
            }
        }
    }

    fun carregarAgendamentos(data: String) {

        _dataSelecionada.value = data

        atualizarLista()
    }

    private fun atualizarLista() {

        val data = _dataSelecionada.value

        _agendamentos.value = cache.filter {
            it.data == data
        }
    }

    fun adicionarAgendamento(
        agendamento: Agendamento
    ) {

        viewModelScope.launch {

            repository.adicionar(agendamento)
        }
    }

    fun excluirAgendamento(id: Int) {

        viewModelScope.launch {

            repository.excluir(id)
        }
    }

    fun atualizarAgendamento(
        agendamento: Agendamento
    ) {

        viewModelScope.launch {

            repository.atualizar(agendamento)
        }
    }

    fun editarAgendamento(
        id: Int,
        petId: Int,
        nomePet: String,
        data: String,
        horario: String,
        descricao: String
    ) {
        viewModelScope.launch {

            val agendamentoOriginal = cache.firstOrNull {
                it.id == id
            } ?: return@launch

            val agendamentoAtualizado = agendamentoOriginal.copy(
                petId = petId,
                nomePet = nomePet,
                data = data,
                horario = horario,
                descricao = descricao
            )

            repository.atualizar(agendamentoAtualizado)
        }
    }

    fun alterarStatusAgendamento(
        agendamento: Agendamento,
        novoStatus: StatusAgendamento
    ) {

        viewModelScope.launch {

            repository.atualizarStatus(
                agendamentoId = agendamento.id,
                petId = agendamento.petId,
                novoStatus = novoStatus
            )
        }
    }
}