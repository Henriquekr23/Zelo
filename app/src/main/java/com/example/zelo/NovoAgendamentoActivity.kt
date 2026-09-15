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

class NovoAgendamentoActivity : AppCompatActivity() {

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
        val nomePetRecebido = intent.getStringExtra("NOME_PET_SELECIONADO")

        if (!nomePetRecebido.isNullOrBlank()) {
            edtNomePet.setText(nomePetRecebido)
        }

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
            val nomePet = edtNomePet.text.toString().trim()
            val data = edtData.text.toString().trim()
            val horario = edtHorario.text.toString().trim()
            val descricao = edtDescricao.text.toString().trim()

            if (
                nomePet.isBlank() ||
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
                putExtra("NOME_PET", nomePet)
                putExtra("DATA", data)
                putExtra("HORARIO", horario)
                putExtra("DESCRICAO", descricao)
            }

            setResult(RESULT_OK, resposta)
            finish()
        }
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
}