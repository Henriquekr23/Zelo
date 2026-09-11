package com.example.zelo

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.zelo.databinding.ActivityAgendaBinding
import com.example.zelo.model.Agendamento
import com.example.zelo.viewmodel.AgendaViewModel

class AgendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendaBinding
    private val viewModel: AgendaViewModel by viewModels()

    private val novoAgendamentoLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado ->

            if (resultado.resultCode == RESULT_OK) {
                val dados = resultado.data ?: return@registerForActivityResult

                val agendamento = Agendamento(
                    id = System.currentTimeMillis().toInt(),
                    petId = 1,
                    nomePet = dados.getStringExtra("NOME_PET").orEmpty(),
                    data = dados.getStringExtra("DATA").orEmpty(),
                    horario = dados.getStringExtra("HORARIO").orEmpty(),
                    descricao = dados.getStringExtra("DESCRICAO").orEmpty()
                )

                viewModel.adicionarAgendamento(agendamento)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAgendaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarObservadores()
        configurarEventos()
    }

    private fun configurarObservadores() {
        viewModel.dataSelecionada.observe(this) { data ->
            binding.txtDataSelecionada.text = data
        }

        viewModel.agendamentos.observe(this) { agendamentos ->
            val resumos = agendamentos.map { it.formatarResumo() }

            binding.listViewAgendamentos.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                resumos
            )
        }
    }

    private fun configurarEventos() {
        binding.btnNovoAgendamento.setOnClickListener {
            val intent = Intent(
                this,
                NovoAgendamentoActivity::class.java
            )

            intent.putExtra(
                "DATA_SELECIONADA",
                viewModel.dataSelecionada.value
            )

            novoAgendamentoLauncher.launch(intent)
        }
    }
}