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
import android.widget.Toast
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.zelo.data.repository.AppThemeRepository
import android.content.res.ColorStateList
import android.graphics.Color
import com.example.zelo.data.repository.AppColorTheme
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AlertDialog
import com.example.zelo.model.StatusAgendamento

class AgendaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendaBinding

    private val viewModel: AgendaViewModel by viewModels()

    private val localeBrasil = Locale("pt", "BR")

    private val formatoData =
        SimpleDateFormat("dd/MM/yyyy", localeBrasil).apply {
            isLenient = false
        }

    private var dataSelecionadaAtual =
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale("pt", "BR")
        ).format(Calendar.getInstance().time)

    private val inicioCalendario: Calendar by lazy {
        Calendar.getInstance(localeBrasil).apply {
            formatoData.parse(dataSelecionadaAtual)?.let {
                time = it
            }

            moverParaSegundaFeira(this)
        }
    }

    private val appThemeRepository by lazy {
        AppThemeRepository(applicationContext)
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

            val nomePet = dados
                .getStringExtra("NOME_PET")
                .orEmpty()

            val petId = dados.getIntExtra("PET_ID", -1)

            if (petId == -1) {

                Toast.makeText(
                    this,
                    "Animal inválido.",
                    Toast.LENGTH_SHORT
                ).show()

                return@registerForActivityResult
            }

            val idAgendamento = dados.getIntExtra(
                "AGENDAMENTO_ID",
                0
            )

            val agendamento = Agendamento(
                id = idAgendamento,
                petId = petId,
                nomePet = nomePet,
                data = dados.getStringExtra("DATA").orEmpty(),
                horario = dados.getStringExtra("HORARIO").orEmpty(),
                descricao = dados.getStringExtra("DESCRICAO").orEmpty()
            )

            if (idAgendamento > 0) {

                viewModel.editarAgendamento(
                    id = idAgendamento,
                    petId = petId,
                    nomePet = nomePet,
                    data = agendamento.data,
                    horario = agendamento.horario,
                    descricao = agendamento.descricao
                )

            } else {

                viewModel.adicionarAgendamento(agendamento)
            }

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

        binding = ActivityAgendaBinding.inflate(layoutInflater)

        setContentView(binding.root)

        configurarEventos()
        configurarObservadores()
        configurarBarraNavegacao()

        configurarDataInicial()
    }

    private fun configurarBarraNavegacao() {

        binding.composeViewBottomNav.setContent {

            val temaSelecionado by
            appThemeRepository.tema.collectAsState()

            ZeloTheme(
                temaCor = temaSelecionado
            ) {

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {

                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = "Início"
                            )
                        },
                        label = {
                            Text("Início")
                        },
                        selected = false,
                        onClick = {
                            navegarParaTela("inicio")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor =
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Agenda"
                            )
                        },
                        label = {
                            Text("Agenda")
                        },
                        selected = true,
                        onClick = {},
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor =
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Filled.List,
                                contentDescription = "Histórico"
                            )
                        },
                        label = {
                            Text("Histórico")
                        },
                        selected = false,
                        onClick = {
                            navegarParaTela("historico")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor =
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    )

                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Perfil"
                            )
                        },
                        label = {
                            Text("Perfil")
                        },
                        selected = false,
                        onClick = {
                            navegarParaTela("perfil")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor =
                                MaterialTheme.colorScheme.secondaryContainer
                        )
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
                    agendamentos = agendamentos,
                    onEditarAgendamento = { agendamento ->
                        exibirOpcoesAgendamento(agendamento)
                    }
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

            val corTexto = if (estaSelecionado) {

                Color.WHITE

            } else {

                when (appThemeRepository.tema.value) {

                    AppColorTheme.ORIGINAL ->
                        Color.rgb(108, 94, 86)

                    AppColorTheme.VERDE ->
                        Color.rgb(83, 102, 91)

                    AppColorTheme.AZUL ->
                        Color.rgb(86, 105, 125)
                }
            }

            setTextColor(corTexto)

            setTypeface(
                null,
                if (estaSelecionado) {
                    Typeface.BOLD
                } else {
                    Typeface.NORMAL
                }
            )

            background = if (estaSelecionado) {

                val corSelecionada = when (
                    appThemeRepository.tema.value
                ) {
                    AppColorTheme.ORIGINAL ->
                        Color.rgb(184, 116, 91)

                    AppColorTheme.VERDE ->
                        Color.rgb(56, 142, 108)

                    AppColorTheme.AZUL ->
                        Color.rgb(57, 120, 184)
                }

                GradientDrawable().apply {

                    shape = GradientDrawable.RECTANGLE

                    cornerRadius = 14.dp().toFloat()

                    setColor(corSelecionada)
                }

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

    private fun configurarDataInicial() {

        val hoje = formatoData.format(Calendar.getInstance().time)
        val dataRecebida = intent.getStringExtra("DATA_AGENDAMENTO")

        val dataInicial = if (dataRecebida.isNullOrBlank()) {
            hoje
        } else {
            val dataValida = try {
                formatoData.parse(dataRecebida)
            } catch (_: Exception) {
                null
            }

            if (dataValida != null && formatoData.format(dataValida) == dataRecebida) {
                dataRecebida
            } else {
                hoje
            }
        }

        dataSelecionadaAtual = dataInicial
        reposicionarCalendario(dataInicial)

        binding.txtDataSelecionada.text = formatarDataCabecalho(dataInicial)
        montarCalendario()

        viewModel.carregarAgendamentos(dataInicial)
    }

    private fun abrirEdicaoAgendamento(agendamento: Agendamento) {

        val intent = Intent(
            this,
            NovoAgendamentoActivity::class.java
        ).apply {
            putExtra("EDITAR_AGENDAMENTO", true)
            putExtra("AGENDAMENTO_ID", agendamento.id)
            putExtra("PET_ID_SELECIONADO", agendamento.petId)
            putExtra("DATA_SELECIONADA", agendamento.data)
            putExtra("HORARIO_SELECIONADO", agendamento.horario)
            putExtra("DESCRICAO_SELECIONADA", agendamento.descricao)
        }

        novoAgendamentoLauncher.launch(intent)
    }

    private fun exibirOpcoesAgendamento(
        agendamento: Agendamento
    ) {

        if (agendamento.status != StatusAgendamento.AGENDADO) {

            val status = when (agendamento.status) {
                StatusAgendamento.CONCLUIDO -> "Concluído"
                StatusAgendamento.CANCELADO -> "Cancelado"
                StatusAgendamento.AGENDADO -> "Agendado"
            }

            AlertDialog.Builder(this)
                .setTitle("Agendamento de ${agendamento.nomePet}")
                .setMessage(
                    "${agendamento.descricao}\n" +
                            "${agendamento.data} às ${agendamento.horario}\n\n" +
                            "Status: $status"
                )
                .setPositiveButton("Fechar", null)
                .show()

            return
        }

        val opcoes = arrayOf(
            "Marcar como concluído",
            "Alterar agendamento",
            "Cancelar agendamento"
        )

        AlertDialog.Builder(this)
            .setTitle("Agendamento de ${agendamento.nomePet}")
            .setItems(opcoes) { _, posicao ->

                when (posicao) {

                    0 -> confirmarAlteracaoStatus(
                        agendamento,
                        StatusAgendamento.CONCLUIDO
                    )

                    1 -> abrirEdicaoAgendamento(
                        agendamento
                    )

                    2 -> confirmarAlteracaoStatus(
                        agendamento,
                        StatusAgendamento.CANCELADO
                    )
                }
            }
            .setNegativeButton("Voltar", null)
            .show()
    }

    private fun confirmarAlteracaoStatus(
        agendamento: Agendamento,
        novoStatus: StatusAgendamento
    ) {

        val acao = when (novoStatus) {
            StatusAgendamento.CONCLUIDO -> "concluir"
            StatusAgendamento.CANCELADO -> "cancelar"
            StatusAgendamento.AGENDADO -> return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar alteração")
            .setMessage(
                "Deseja realmente $acao o agendamento de ${agendamento.nomePet}?"
            )
            .setNegativeButton("Voltar", null)
            .setPositiveButton("Confirmar") { _, _ ->

                viewModel.alterarStatusAgendamento(
                    agendamento,
                    novoStatus
                )
            }
            .show()
    }

    private fun aplicarTemaAgenda() {

        val tema = appThemeRepository.tema.value

        val corPrimaria = when (tema) {
            AppColorTheme.ORIGINAL -> Color.rgb(184, 116, 91)
            AppColorTheme.VERDE -> Color.rgb(56, 142, 108)
            AppColorTheme.AZUL -> Color.rgb(57, 120, 184)
        }

        val corFundo = when (tema) {
            AppColorTheme.ORIGINAL -> Color.rgb(250, 247, 243)
            AppColorTheme.VERDE -> Color.rgb(247, 251, 248)
            AppColorTheme.AZUL -> Color.rgb(247, 250, 254)
        }

        binding.root.setBackgroundColor(corFundo)

        binding.btnNovoAgendamento.backgroundTintList =
            ColorStateList.valueOf(corPrimaria)

        window.statusBarColor = corPrimaria

        montarCalendario()
    }

    override fun onResume() {
        super.onResume()
        aplicarTemaAgenda()
    }
}