package com.example.zelo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.example.zelo.adapter.PetsAdapter
import com.example.zelo.databinding.ActivityMeusPetsBinding
import com.example.zelo.model.Pet
import com.example.zelo.ui.theme.ZeloTheme
import com.example.zelo.viewmodel.PetsViewModel
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import java.util.Calendar
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.TextView
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.ScrollView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.zelo.model.Agendamento
import com.example.zelo.viewmodel.AgendaViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.zelo.databinding.DialogDetalhesPetBinding
import com.example.zelo.databinding.ItemAgendamentoDetalhesBinding
import com.example.zelo.model.StatusAgendamento
import android.graphics.Color
import android.content.res.ColorStateList
import android.view.ViewGroup
import com.example.zelo.data.repository.AppColorTheme
import com.example.zelo.data.repository.AppThemeRepository
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

/**
 * Tela "Meus Pets".
 *
 * Demonstra o uso de:
 *  - GridView (grade de cards com os pets do tutor);
 *  - View Binding (inflar e acessar as views do layout);
 *  - MVVM (PetsViewModel + PetsModel fornecendo os dados);
 *  - Navegação entre telas com passagem de parâmetros via Intent
 *    (o pet selecionado é enviado para NovoAgendamentoActivity).
 */
class MeusPetsActivity : AppCompatActivity() {

    private fun confirmarExclusao(pet: Pet) {

        AlertDialog.Builder(this)
            .setTitle("Excluir ${pet.nome}?")
            .setMessage(
                "Deseja realmente excluir este animal?\n\n" +
                        "Esta ação não poderá ser desfeita."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->

                viewModel.excluirPet(pet.id) { excluido ->

                    if (excluido) {

                        Toast.makeText(
                            this,
                            "${pet.nome} foi excluído",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        Toast.makeText(
                            this,
                            "Não foi possível excluir o animal. " +
                                    "Verifique se ele possui agendamentos.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .show()
    }

    private fun abrirOpcoesPet(pet: Pet) {

        val opcoes = arrayOf(
            "Editar informações",
            "Excluir animal"
        )

        AlertDialog.Builder(this)
            .setTitle("${pet.emoji} ${pet.nome}")
            .setItems(opcoes) { _, opcao ->

                when (opcao) {

                    0 -> abrirFormularioEdicao(pet)

                    1 -> confirmarExclusao(pet)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun abrirFormularioEdicao(pet: Pet) {

        val layout = LinearLayout(this).apply {

            orientation = LinearLayout.VERTICAL

            val padding =
                (24 * resources.displayMetrics.density).toInt()

            setPadding(
                padding,
                padding,
                padding,
                padding
            )
        }

        // Nome
        val campoNome = EditText(this).apply {

            hint = "Nome do pet"

            setText(pet.nome)

            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        // Espécie
        val campoEspecie = EditText(this).apply {

            hint = "Espécie"

            setText(pet.especie)

            inputType = InputType.TYPE_CLASS_TEXT
        }

        // Raça
        val campoRaca = EditText(this).apply {

            hint = "Raça"

            setText(pet.raca)

            inputType = InputType.TYPE_CLASS_TEXT
        }

        // Ano de nascimento
        val campoAno = EditText(this).apply {

            hint = "Ano de nascimento"

            setText(pet.anoNascimento.toString())

            inputType = InputType.TYPE_CLASS_NUMBER
        }

        // Título do seletor
        val tituloEmoji = TextView(this).apply {

            text = "Escolha o animal:"

            textSize = 16f

            setPadding(0, 24, 0, 12)
        }

        // Grupo de emojis
        val grupoEmojis = RadioGroup(this).apply {

            orientation = RadioGroup.HORIZONTAL

            gravity = Gravity.CENTER
        }

        val emojis = listOf(
            "🐶",
            "🐱",
            "🐦",
            "🐭"
        )

        emojis.forEach { emoji ->

            val botao = RadioButton(this).apply {

                id = View.generateViewId()

                text = emoji

                textSize = 26f

                gravity = Gravity.CENTER

                isChecked = emoji == pet.emoji
            }

            grupoEmojis.addView(botao)
        }

        // Selecionar opção inicial
        if (grupoEmojis.checkedRadioButtonId == -1) {

            grupoEmojis.check(
                grupoEmojis.getChildAt(0).id
            )
        }

        // Adicionar campos
        layout.addView(campoNome)
        layout.addView(campoEspecie)
        layout.addView(campoRaca)
        layout.addView(campoAno)
        layout.addView(tituloEmoji)
        layout.addView(grupoEmojis)

        // Permitir rolagem
        val scrollView = ScrollView(this).apply {

            addView(layout)
        }

        // Criar diálogo
        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar ${pet.nome}")
            .setView(scrollView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val nome = campoNome.text
                    .toString()
                    .trim()

                val especie = campoEspecie.text
                    .toString()
                    .trim()

                val raca = campoRaca.text
                    .toString()
                    .trim()

                val ano = campoAno.text
                    .toString()
                    .toIntOrNull()

                val emoji = grupoEmojis
                    .findViewById<RadioButton>(
                        grupoEmojis.checkedRadioButtonId
                    )
                    ?.text
                    ?.toString()
                    ?: "🐾"

                // Validações
                if (nome.isBlank()) {

                    campoNome.error = "Informe o nome"

                    return@setOnClickListener
                }

                if (especie.isBlank()) {

                    campoEspecie.error = "Informe a espécie"

                    return@setOnClickListener
                }

                val anoAtual = Calendar.getInstance()
                    .get(Calendar.YEAR)

                if (ano == null || ano !in 1900..anoAtual) {

                    campoAno.error =
                        "Informe um ano válido"

                    return@setOnClickListener
                }

                // Atualizar o pet
                viewModel.atualizarPet(
                    id = pet.id,
                    nome = nome,
                    especie = especie,
                    raca = raca,
                    anoNascimento = ano,
                    emoji = emoji
                )

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private lateinit var binding: ActivityMeusPetsBinding

    private val viewModel: PetsViewModel by viewModels()
    private val agendaViewModel: AgendaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMeusPetsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarEventos()
        configurarObservadores()
        configurarBarraNavegacao()
    }

    private fun configurarObservadores() {

        viewModel.pets.observe(this) { pets ->

            binding.gridViewPets.adapter = PetsAdapter(
                context = this,
                pets = pets,
                calcularIdade = viewModel::calcularIdade
            )

            // Toque simples: agendamento
            binding.gridViewPets.setOnItemClickListener {
                    _, _, posicao, _ ->

                val petSelecionado = pets[posicao]

                abrirDetalhesPet(petSelecionado)
            }

            // Toque prolongado: editar ou excluir
            binding.gridViewPets.setOnItemLongClickListener {
                    _, _, posicao, _ ->

                val petSelecionado = pets[posicao]

                abrirOpcoesPet(petSelecionado)

                true
            }
        }
    }

    private fun configurarEventos() {

        binding.btnVoltarMeusPets.setOnClickListener {
            finish()
        }

        binding.btnAdicionarPet.setOnClickListener {
            abrirFormularioCadastro()
        }
    }

    /**
     * Envia o pet clicado no GridView para a tela de novo agendamento
     * através de extras no Intent — o nome do pet chega pré-preenchido
     * na tela de destino (NovoAgendamentoActivity).
     */
    private fun abrirAgendamentoParaPet(pet: Pet) {

        val intent = Intent(
            this,
            NovoAgendamentoActivity::class.java
        ).apply {

            putExtra("PET_ID_SELECIONADO", pet.id)

            putExtra("NOME_PET_SELECIONADO", pet.nome)
        }

        novoAgendamentoLauncher.launch(intent)
    }

    private fun configurarBarraNavegacao() {
        binding.composeViewBottomNavPets.setContent {

            val temaSelecionado by
            appThemeRepository.tema.collectAsState()

            ZeloTheme(
                temaCor = temaSelecionado
            ) {

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
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
                        selected = false,
                        onClick = {
                            startActivity(Intent(this@MeusPetsActivity, AgendaActivity::class.java))
                        }
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
                        selected = true,
                        onClick = { /* Já estamos em Meus Pets, dentro do fluxo de Perfil */ }
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

    private fun abrirFormularioCadastro() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            val padding = (24 * resources.displayMetrics.density).toInt()

            setPadding(
                padding,
                padding,
                padding,
                padding
            )
        }

        val campoNome = EditText(this).apply {
            hint = "Nome do pet"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }

        val campoEspecie = EditText(this).apply {
            hint = "Espécie (Ex.: Gato, Cachorro)"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val campoRaca = EditText(this).apply {
            hint = "Raça"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        val campoAnoNascimento = EditText(this).apply {
            hint = "Ano de nascimento (Ex.: 2023)"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        // Título da seleção
        val tituloEmoji = TextView(this).apply {
            text = "Escolha o animal:"
            textSize = 16f

            setPadding(
                0,
                24,
                0,
                12
            )
        }

// Grupo de seleção exclusiva
        val grupoEmojis = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
            gravity = Gravity.CENTER
        }

// Emojis disponíveis
        val emojis = listOf(
            "🐶",
            "🐱",
            "🐦",
            "🐭"
        )

// Criar um botão para cada emoji
        emojis.forEach { emoji ->

            val botao = RadioButton(this).apply {

                id = android.view.View.generateViewId()

                text = emoji
                textSize = 28f

                gravity = Gravity.CENTER

                setPadding(
                    8,
                    8,
                    8,
                    8
                )
            }

            grupoEmojis.addView(botao)
        }

// Selecionar cachorro por padrão
        grupoEmojis.check(
            grupoEmojis.getChildAt(0).id
        )

        layout.addView(campoNome)
        layout.addView(campoEspecie)
        layout.addView(campoRaca)
        layout.addView(campoAnoNascimento)
        layout.addView(tituloEmoji)
        layout.addView(grupoEmojis)

        val scrollView = android.widget.ScrollView(this).apply {
            isFillViewport = false
            addView(layout)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Cadastrar novo pet")
            .setView(scrollView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Cadastrar", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener {

                val nome = campoNome.text.toString().trim()
                val especie = campoEspecie.text.toString().trim()
                val raca = campoRaca.text.toString().trim()

                val anoNascimento = campoAnoNascimento.text
                    .toString()
                    .toIntOrNull()

                val emoji = grupoEmojis
                    .findViewById<RadioButton>(
                        grupoEmojis.checkedRadioButtonId
                    )
                    ?.text
                    ?.toString()
                    ?: "🐾"

                if (nome.isBlank()) {

                    campoNome.error = "Informe o nome do pet"

                    return@setOnClickListener
                }

                if (especie.isBlank()) {

                    campoEspecie.error = "Informe a espécie"

                    return@setOnClickListener
                }

                val anoAtual = Calendar.getInstance()
                    .get(Calendar.YEAR)

                if (anoNascimento == null ||
                    anoNascimento !in 1900..anoAtual
                ) {

                    campoAnoNascimento.error =
                        "Informe um ano válido"

                    return@setOnClickListener
                }

                viewModel.cadastrarPet(
                    nome = nome,
                    especie = especie,
                    raca = raca,
                    anoNascimento = anoNascimento,
                    emoji = emoji
                )

                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private val novoAgendamentoLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { resultado ->

            if (resultado.resultCode != RESULT_OK) {
                return@registerForActivityResult
            }

            val dados = resultado.data
                ?: return@registerForActivityResult

            val petId = dados.getIntExtra("PET_ID", -1)

            if (petId == -1) {

                Toast.makeText(
                    this,
                    "Animal inválido.",
                    Toast.LENGTH_SHORT
                ).show()

                return@registerForActivityResult
            }

            val agendamento = Agendamento(
                id = 0,
                petId = petId,
                nomePet = dados.getStringExtra("NOME_PET").orEmpty(),
                data = dados.getStringExtra("DATA").orEmpty(),
                horario = dados.getStringExtra("HORARIO").orEmpty(),
                descricao = dados.getStringExtra("DESCRICAO").orEmpty()
            )

            agendaViewModel.adicionarAgendamento(agendamento)

            Toast.makeText(
                this,
                "Agendamento solicitado com sucesso!",
                Toast.LENGTH_SHORT
            ).show()
        }

    private fun abrirDetalhesPet(pet: Pet) {

        viewModel.buscarAgendamentosDoPet(pet.id) { agendamentos ->

            if (isFinishing || isDestroyed) {
                return@buscarAgendamentosDoPet
            }

            val dialogBinding = DialogDetalhesPetBinding.inflate(layoutInflater)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogBinding.root)
                .create()

            dialogBinding.txtEmojiDetalhes.text = pet.emoji
            dialogBinding.txtNomeDetalhes.text = pet.nome

            dialogBinding.txtRacaDetalhes.text =
                "${pet.especie} • ${pet.raca}"

            dialogBinding.txtIdadeDetalhes.text =
                "${viewModel.calcularIdade(pet.anoNascimento)} anos"

            dialogBinding.txtEspecieDetalhes.text = pet.especie

            val container = dialogBinding.layoutAgendamentosDetalhes
            container.removeAllViews()

            if (agendamentos.isEmpty()) {

                val mensagem = TextView(this).apply {
                    text = "Nenhum agendamento encontrado."
                    textSize = 14f
                    gravity = Gravity.CENTER
                    setPadding(16, 28, 16, 28)
                }

                container.addView(mensagem)

            } else {

                val formato = SimpleDateFormat(
                    "dd/MM/yyyy",
                    Locale("pt", "BR")
                ).apply {
                    isLenient = false
                }

                val agendamentosOrdenados = agendamentos.sortedWith(
                    compareBy(
                        { runCatching { formato.parse(it.data)?.time }.getOrNull() ?: Long.MAX_VALUE },
                        { it.horario }
                    )
                )

                agendamentosOrdenados.forEach { agendamento ->

                    val itemBinding = ItemAgendamentoDetalhesBinding.inflate(
                        layoutInflater,
                        container,
                        false
                    )

                    itemBinding.txtDataAgendamentoDetalhes.text =
                        "📅 ${agendamento.data}"

                    itemBinding.txtHorarioAgendamentoDetalhes.text =
                        "🕒 ${agendamento.horario}"

                    itemBinding.txtDescricaoAgendamentoDetalhes.text =
                        agendamento.descricao

                    val statusTexto = itemBinding.txtStatusAgendamentoDetalhes

                    when (agendamento.status) {

                        StatusAgendamento.AGENDADO -> {
                            statusTexto.text = "AGENDADO"
                            statusTexto.setTextColor(
                                Color.parseColor("#B45309")
                            )
                        }

                        StatusAgendamento.CONCLUIDO -> {
                            statusTexto.text = "CONCLUÍDO"
                            statusTexto.setTextColor(
                                Color.parseColor("#15803D")
                            )
                        }

                        StatusAgendamento.CANCELADO -> {
                            statusTexto.text = "CANCELADO"
                            statusTexto.setTextColor(
                                Color.parseColor("#B91C1C")
                            )
                        }
                    }

// Permitir gerenciar somente agendamentos pendentes
                    if (agendamento.status == StatusAgendamento.AGENDADO) {

                        itemBinding.root.setOnClickListener {

                            dialog.dismiss()

                            abrirOpcoesAgendamento(
                                pet,
                                agendamento
                            )
                        }

                        itemBinding.root.isClickable = true
                        itemBinding.root.isFocusable = true
                    }

                    container.addView(itemBinding.root)
                }
            }

            dialogBinding.btnNovoAgendamentoDetalhes.setOnClickListener {
                dialog.dismiss()
                abrirAgendamentoParaPet(pet)
            }

            dialogBinding.btnFecharDetalhes.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

    }

    private fun abrirOpcoesAgendamento(
        pet: Pet,
        agendamento: Agendamento
    ) {

        if (agendamento.status != StatusAgendamento.AGENDADO) {
            return
        }

        val opcoes = arrayOf(
            "Marcar como concluído",
            "Cancelar agendamento"
        )

        AlertDialog.Builder(this)
            .setTitle(agendamento.descricao)
            .setItems(opcoes) { _, posicao ->

                val novoStatus = when (posicao) {

                    0 -> StatusAgendamento.CONCLUIDO

                    1 -> StatusAgendamento.CANCELADO

                    else -> return@setItems
                }

                confirmarAlteracaoStatus(
                    pet,
                    agendamento,
                    novoStatus
                )
            }
            .setNegativeButton("Voltar", null)
            .show()
    }

    private fun confirmarAlteracaoStatus(
        pet: Pet,
        agendamento: Agendamento,
        novoStatus: StatusAgendamento
    ) {

        val acao = when (novoStatus) {
            StatusAgendamento.CONCLUIDO -> "concluir"
            StatusAgendamento.CANCELADO -> "cancelar"
            else -> return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar alteração")
            .setMessage(
                "Deseja realmente $acao este agendamento?"
            )
            .setNegativeButton("Voltar", null)
            .setPositiveButton("Confirmar") { _, _ ->

                viewModel.alterarStatusAgendamento(
                    agendamentoId = agendamento.id,
                    petId = pet.id,
                    novoStatus = novoStatus
                ) { atualizado ->

                    if (isFinishing || isDestroyed) {
                        return@alterarStatusAgendamento
                    }

                    if (atualizado) {

                        Toast.makeText(
                            this,
                            "Status atualizado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()

                        abrirDetalhesPet(pet)

                    } else {

                        Toast.makeText(
                            this,
                            "Não foi possível atualizar o agendamento.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .show()
    }

    private val appThemeRepository by lazy {
        AppThemeRepository(applicationContext)
    }

    private fun aplicarTemaMeusPets() {

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

        val corTextoSecundario = when (tema) {
            AppColorTheme.ORIGINAL -> Color.rgb(240, 232, 224)
            AppColorTheme.VERDE -> Color.rgb(224, 241, 231)
            AppColorTheme.AZUL -> Color.rgb(225, 238, 251)
        }

        binding.root.setBackgroundColor(corFundo)

        binding.layoutHeaderMeusPets.setBackgroundColor(corPrimaria)

        binding.btnAdicionarPet.backgroundTintList =
            ColorStateList.valueOf(corPrimaria)

        binding.btnAdicionarPet.setTextColor(Color.WHITE)

        binding.btnVoltarMeusPets.setTextColor(Color.WHITE)

        binding.txtTituloMeusPets.setTextColor(Color.WHITE)

        binding.txtSubtituloMeusPets.setTextColor(
            corTextoSecundario
        )

        window.statusBarColor = corPrimaria
    }

    override fun onResume() {
        super.onResume()
        aplicarTemaMeusPets()
    }
}
