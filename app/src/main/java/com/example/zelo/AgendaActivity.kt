package com.example.zelo

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.adapter.AgendaAdapter
import com.example.zelo.databinding.ActivityAgendaBinding
import com.example.zelo.model.Agendamento
import com.example.zelo.viewmodel.AgendaViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendaBinding

    private val viewModel: AgendaViewModel by viewModels()

    private val localeBrasil = Locale("pt", "BR")

    private val formatoData =
        SimpleDateFormat("dd/MM/yyyy", localeBrasil).apply {
            isLenient = false
        }

    private var dataSelecionadaAtual = "10/09/2026"

    private val inicioCalendario: Calendar by lazy {
        Calendar.getInstance(localeBrasil).apply {
            formatoData.parse(dataSelecionadaAtual)?.let {
                time = it
            }

            moverParaSegundaFeira(this)
        }
    }

    private val novoAgendamentoLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado ->

            if (resultado.resultCode != RESULT_OK) {
                return@registerForActivityResult
            }

            val dados =
                resultado.data ?: return@registerForActivityResult

            val agendamento = Agendamento(
                id = System.currentTimeMillis().toInt(),
                petId = 1,
                nomePet = dados
                    .getStringExtra("NOME_PET")
                    .orEmpty(),
                data = dados
                    .getStringExtra("DATA")
                    .orEmpty(),
                horario = dados
                    .getStringExtra("HORARIO")
                    .orEmpty(),
                descricao = dados
                    .getStringExtra("DESCRICAO")
                    .orEmpty()
            )

            viewModel.adicionarAgendamento(agendamento)

            dataSelecionadaAtual = agendamento.data

            reposicionarCalendario(
                agendamento.data
            )

            viewModel.carregarAgendamentos(
                agendamento.data
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityAgendaBinding.inflate(layoutInflater)

        setContentView(binding.root)

        configurarEventos()
        configurarObservadores()
        configurarBarraNavegacao()
    }

    private fun configurarBarraNavegacao() {
        binding.composeViewBottomNav.setContent {
            ZeloTheme {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                        label = { Text("Início") },
                        selected = false,
                        onClick = { navegarParaTela("inicio") },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Agenda") },
                        label = { Text("Agenda") },
                        selected = true,
                        onClick = { /* Já estamos na agenda */ }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Histórico") },
                        label = { Text("Histórico") },
                        selected = false,
                        onClick = { navegarParaTela("historico") },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                        label = { Text("Perfil") },
                        selected = false,
                        onClick = { navegarParaTela("perfil") },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.secondaryContainer)
                    )
                }
            }
        }
    }

    private fun navegarParaTela(tela: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("TELA", tela)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }

    private fun configurarObservadores() {
        viewModel.dataSelecionada.observe(this) { data ->
            dataSelecionadaAtual = data

            binding.txtDataSelecionada.text =
                formatarDataCabecalho(data)

            montarCalendario()
        }

        viewModel.agendamentos.observe(this) {
                agendamentos ->

            binding.txtQuantidade.text =
                formatarQuantidade(agendamentos.size)

            binding.listViewAgendamentos.adapter =
                AgendaAdapter(
                    context = this,
                    agendamentos = agendamentos
                )
        }
    }

    private fun configurarEventos() {
        binding.btnNovoAgendamento.setOnClickListener {
            abrirNovoAgendamento()
        }

        binding.btnSemanaAnterior.setOnClickListener {
            inicioCalendario.add(
                Calendar.DAY_OF_MONTH,
                -7
            )

            montarCalendario()

            binding.scrollCalendario.smoothScrollTo(
                0,
                0
            )
        }

        binding.btnProximaSemana.setOnClickListener {
            inicioCalendario.add(
                Calendar.DAY_OF_MONTH,
                7
            )

            montarCalendario()

            binding.scrollCalendario.smoothScrollTo(
                0,
                0
            )
        }
    }

    private fun montarCalendario() {
        binding.layoutDias.removeAllViews()

        atualizarNomeDoMes()

        repeat(14) { posicao ->
            val dia =
                inicioCalendario.clone() as Calendar

            dia.add(
                Calendar.DAY_OF_MONTH,
                posicao
            )

            adicionarDiaAoCalendario(dia)
        }
    }

    private fun adicionarDiaAoCalendario(
        dia: Calendar
    ) {
        val dataDoDia =
            formatoData.format(dia.time)

        val estaSelecionado =
            dataDoDia == dataSelecionadaAtual

        val formatoDiaSemana =
            SimpleDateFormat("EEE", localeBrasil)

        val nomeDia =
            formatoDiaSemana
                .format(dia.time)
                .replace(".", "")
                .uppercase(localeBrasil)

        val numeroDia =
            dia.get(Calendar.DAY_OF_MONTH)

        val parametros =
            LinearLayout.LayoutParams(
                52.dp(),
                64.dp()
            ).apply {
                marginStart = 2.dp()
                marginEnd = 2.dp()
            }

        val textViewDia = TextView(this).apply {
            layoutParams = parametros

            gravity = Gravity.CENTER

            text = "$nomeDia\n$numeroDia"

            textSize = 12f

            setPadding(
                4.dp(),
                6.dp(),
                4.dp(),
                6.dp()
            )

            setTextColor(
                ContextCompat.getColor(
                    this@AgendaActivity,
                    if (estaSelecionado) {
                        android.R.color.white
                    } else {
                        R.color.zelo_text_secondary
                    }
                )
            )

            setTypeface(
                null,
                if (estaSelecionado) {
                    Typeface.BOLD
                } else {
                    Typeface.NORMAL
                }
            )

            background = if (estaSelecionado) {
                ContextCompat.getDrawable(
                    this@AgendaActivity,
                    R.drawable.bg_dia_selecionado
                )
            } else {
                null
            }

            setOnClickListener {
                dataSelecionadaAtual = dataDoDia

                viewModel.carregarAgendamentos(
                    dataDoDia
                )
            }
        }

        binding.layoutDias.addView(textViewDia)
    }

    private fun atualizarNomeDoMes() {
        val formatoMes =
            SimpleDateFormat(
                "MMMM yyyy",
                localeBrasil
            )

        val nomeMes =
            formatoMes
                .format(inicioCalendario.time)
                .replaceFirstChar { caractere ->
                    if (caractere.isLowerCase()) {
                        caractere.titlecase(localeBrasil)
                    } else {
                        caractere.toString()
                    }
                }

        binding.txtMesAtual.text = nomeMes
    }

    private fun reposicionarCalendario(
        data: String
    ) {
        val dataConvertida = try {
            formatoData.parse(data)
        } catch (_: Exception) {
            null
        }

        if (dataConvertida == null) {
            return
        }

        inicioCalendario.time = dataConvertida

        moverParaSegundaFeira(
            inicioCalendario
        )
    }

    private fun moverParaSegundaFeira(
        calendario: Calendar
    ) {
        val deslocamento =
            (
                    calendario.get(Calendar.DAY_OF_WEEK) -
                            Calendar.MONDAY +
                            7
                    ) % 7

        calendario.add(
            Calendar.DAY_OF_MONTH,
            -deslocamento
        )
    }

    private fun formatarQuantidade(
        quantidade: Int
    ): String {
        return if (quantidade == 1) {
            "1 agendamento"
        } else {
            "$quantidade agendamentos"
        }
    }

    private fun formatarDataCabecalho(
        data: String
    ): String {
        val dataConvertida = try {
            formatoData.parse(data)
        } catch (_: Exception) {
            null
        }

        if (dataConvertida == null) {
            return data
        }

        val formatoCabecalho =
            SimpleDateFormat(
                "EEEE, dd 'de' MMMM",
                localeBrasil
            )

        return formatoCabecalho
            .format(dataConvertida)
            .uppercase(localeBrasil)
    }

    private fun abrirNovoAgendamento() {
        val intent = Intent(
            this,
            NovoAgendamentoActivity::class.java
        )

        intent.putExtra(
            "DATA_SELECIONADA",
            viewModel.dataSelecionada.value
                ?: dataSelecionadaAtual
        )

        novoAgendamentoLauncher.launch(intent)
    }

    private fun Int.dp(): Int {
        return (
                this * resources.displayMetrics.density
                ).toInt()
    }
}