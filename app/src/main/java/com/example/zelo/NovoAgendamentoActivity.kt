package com.example.zelo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class NovoAgendamentoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_novo_agendamento)

        val edtNomePet =
            findViewById<EditText>(R.id.edtNomePet)

        val edtData =
            findViewById<EditText>(R.id.edtData)

        val edtHorario =
            findViewById<EditText>(R.id.edtHorario)

        val edtDescricao =
            findViewById<EditText>(R.id.edtDescricao)

        val btnSalvar =
            findViewById<Button>(R.id.btnSalvar)

        val dataRecebida =
            intent.getStringExtra("DATA_SELECIONADA")

        edtData.setText(dataRecebida)

        btnSalvar.setOnClickListener {
            val resposta = Intent().apply {
                putExtra("NOME_PET", edtNomePet.text.toString())
                putExtra("DATA", edtData.text.toString())
                putExtra("HORARIO", edtHorario.text.toString())
                putExtra("DESCRICAO", edtDescricao.text.toString())
            }

            setResult(RESULT_OK, resposta)
            finish()
        }
    }
}