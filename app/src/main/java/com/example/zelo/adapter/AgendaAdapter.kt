package com.example.zelo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.zelo.R
import com.example.zelo.model.Agendamento
import com.example.zelo.model.StatusAgendamento

class AgendaAdapter(
    context: Context,
    agendamentos: List<Agendamento>
) : ArrayAdapter<Agendamento>(
    context,
    0,
    agendamentos
) {

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = convertView ?: LayoutInflater
            .from(context)
            .inflate(
                R.layout.item_agendamento,
                parent,
                false
            )

        val agendamento = getItem(position)
            ?: return view

        val txtHorarioTitulo =
            view.findViewById<TextView>(R.id.txtHorarioTitulo)

        val txtStatus =
            view.findViewById<TextView>(R.id.txtStatus)

        val txtPet =
            view.findViewById<TextView>(R.id.txtPetAgenda)

        txtHorarioTitulo.text =
            "${agendamento.horario} — ${agendamento.descricao}"

        txtPet.text =
            "${agendamento.nomePet} • ${agendamento.data}"

        txtStatus.text = when (agendamento.status) {
            StatusAgendamento.AGENDADO -> "CONFIRMADO"
            StatusAgendamento.CONCLUIDO -> "CONCLUÍDO"
            StatusAgendamento.CANCELADO -> "CANCELADO"
        }

        return view
    }
}