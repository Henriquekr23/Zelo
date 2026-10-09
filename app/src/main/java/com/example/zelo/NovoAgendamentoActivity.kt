package com.example.zelo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import com.example.zelo.data.local.PetEntity
import com.example.zelo.data.local.ZeloDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.res.ColorStateList
import android.graphics.Color
import com.example.zelo.data.repository.AppColorTheme
import com.example.zelo.data.repository.AppThemeRepository
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputLayout


class NovoAgendamentoActivity : AppCompatActivity() {

    private val appThemeRepository by lazy {
        AppThemeRepository(applicationContext)
    }

    private val localeBrasil = Locale("pt", "BR")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_agendamento)

        val edtNomePet = findViewById<EditText>(R.id.edtNomePet)
        val edtData = findViewById<EditText>(R.id.edtData)
        val edtHorario = findViewById<EditText>(R.id.edtHorario)
        val edtDescricao = findViewById<EditText>(R.id.edtDescricao)

        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        val btnVoltar = findViewById<Button>(R.id.btnVoltar)

        // Recebe a data selecionada na tela da agenda.
        val dataRecebida = intent.getStringExtra("DATA_SELECIONADA")

        if (!dataRecebida.isNullOrBlank()) {
            edtData.setText(dataRecebida)
        }

        // Recebe o nome do pet selecionado na grade "Meus Pets" (passagem de parâmetro via Intent).

        btnVoltar.setOnClickListener {
            finish()
        }

        edtData.setOnClickListener {
            abrirSeletorData(edtData)
        }

        edtHorario.setOnClickListener {
            abrirSeletorHorario(edtHorario)
        }

        btnSalvar.setOnClickListener {

            val pet = petSelecionado

            if (pet == null) {

                Toast.makeText(
                    this,
                    "Selecione um animal.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val data = edtData.text.toString().trim()
            val horario = edtHorario.text.toString().trim()
            val descricao = edtDescricao.text.toString().trim()

            if (
                data.isBlank() ||
                horario.isBlank() ||
                descricao.isBlank()
            ) {

                Toast.makeText(
                    this,
                    "Preencha todos os campos.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val resposta = Intent().apply {

                putExtra("PET_ID", pet.id)

                putExtra("NOME_PET", pet.nome)

                putExtra("DATA", data)

                putExtra("HORARIO", horario)

                putExtra("DESCRICAO", descricao)
            }

            setResult(RESULT_OK, resposta)

            finish()
        }

        edtNomePet.apply {
            isFocusable = false
            isClickable = true
            isCursorVisible = false

            setOnClickListener {
                abrirSeletorPets(this)
            }
        }

        carregarPets(edtNomePet)
    }

    private fun abrirSeletorData(campoData: EditText) {
        val calendario = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                val dataFormatada = String.format(
                    localeBrasil,
                    "%02d/%02d/%04d",
                    dia,
                    mes + 1,
                    ano
                )

                campoData.setText(dataFormatada)
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun abrirSeletorHorario(campoHorario: EditText) {
        val calendario = Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hora, minuto ->
                val horarioFormatado = String.format(
                    localeBrasil,
                    "%02d:%02d",
                    hora,
                    minuto
                )

                campoHorario.setText(horarioFormatado)
            },
            calendario.get(Calendar.HOUR_OF_DAY),
            calendario.get(Calendar.MINUTE),
            true
        ).show()
    }

    private var petSelecionado: PetEntity? = null

    private lateinit var petsDisponiveis: List<PetEntity>

    private fun carregarPets(campoNomePet: EditText) {

        lifecycleScope.launch {

            val dao = ZeloDatabase
                .getInstance(applicationContext)
                .petDao()

            petsDisponiveis = dao.observarTodos().first()

            val petIdRecebido = intent.getIntExtra(
                "PET_ID_SELECIONADO",
                -1
            )

            if (petIdRecebido != -1) {

                val pet = petsDisponiveis.firstOrNull {
                    it.id == petIdRecebido
                }

                if (pet != null) {

                    petSelecionado = pet
                    campoNomePet.setText(pet.nome)
                }
            }
        }
    }

    private fun abrirSeletorPets(campoNomePet: EditText) {

        if (!::petsDisponiveis.isInitialized) {

            Toast.makeText(
                this,
                "Carregando animais...",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (petsDisponiveis.isEmpty()) {

            Toast.makeText(
                this,
                "Cadastre um animal primeiro.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val nomesPets = petsDisponiveis.map {
            "${it.emoji} ${it.nome} (${it.especie})"
        }.toTypedArray()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Selecione um animal")
            .setItems(nomesPets) { _, posicao ->

                val pet = petsDisponiveis[posicao]

                petSelecionado = pet

                campoNomePet.setText(pet.nome)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun aplicarTemaNovoAgendamento() {

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

        val corTexto = when (tema) {
            AppColorTheme.ORIGINAL -> Color.rgb(55, 48, 43)
            AppColorTheme.VERDE -> Color.rgb(26, 41, 33)
            AppColorTheme.AZUL -> Color.rgb(26, 39, 53)
        }

        val corSecundaria = when (tema) {
            AppColorTheme.ORIGINAL -> Color.rgb(108, 94, 86)
            AppColorTheme.VERDE -> Color.rgb(83, 102, 91)
            AppColorTheme.AZUL -> Color.rgb(86, 105, 125)
        }

        findViewById<android.view.View>(
            R.id.scrollNovoAgendamento
        ).setBackgroundColor(corFundo)

        findViewById<android.view.View>(
            R.id.layoutCabecalhoNovoAgendamento
        ).setBackgroundColor(corPrimaria)

        val btnSalvar = findViewById<Button>(
            R.id.btnSalvar
        )

        btnSalvar.backgroundTintList =
            ColorStateList.valueOf(corPrimaria)

        val btnVoltar = findViewById<Button>(
            R.id.btnVoltar
        )

        btnVoltar.setTextColor(Color.WHITE)

        val campos = listOf(
            findViewById<EditText>(R.id.edtNomePet),
            findViewById<EditText>(R.id.edtData),
            findViewById<EditText>(R.id.edtHorario),
            findViewById<EditText>(R.id.edtDescricao)
        )

        campos.forEach { campo ->
            campo.setTextColor(corTexto)
            campo.setHintTextColor(corSecundaria)
            campo.backgroundTintList = null
        }

        val layouts = listOf(
            findViewById<TextInputLayout>(R.id.layoutNomePet),
            findViewById<TextInputLayout>(R.id.layoutData),
            findViewById<TextInputLayout>(R.id.layoutHorario),
            findViewById<TextInputLayout>(R.id.layoutDescricao)
        )

        layouts.forEach { layout ->

            layout.boxBackgroundColor = Color.WHITE

            layout.boxStrokeColor = corPrimaria

            layout.defaultHintTextColor =
                ColorStateList.valueOf(corSecundaria)

            layout.hintTextColor =
                ColorStateList.valueOf(corPrimaria)
        }

        window.statusBarColor = corPrimaria
    }

    override fun onResume() {
        super.onResume()
        aplicarTemaNovoAgendamento()
    }
}