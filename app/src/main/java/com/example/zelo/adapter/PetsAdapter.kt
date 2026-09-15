package com.example.zelo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.zelo.R
import com.example.zelo.model.Pet

/**
 * Adapter do GridView de "Meus Pets".
 * Cada célula do grid exibe um card com os dados de um Pet.
 *
 * @param calcularIdade função (recebe o ano de nascimento e retorna a idade)
 * fornecida pelo ViewModel, mantendo a regra de negócio fora do adapter.
 */
class PetsAdapter(
    private val context: Context,
    private val pets: List<Pet>,
    private val calcularIdade: (Int) -> Int
) : BaseAdapter() {

    override fun getCount(): Int = pets.size

    override fun getItem(position: Int): Pet = pets[position]

    override fun getItemId(position: Int): Long = pets[position].id.toLong()

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = convertView ?: LayoutInflater
            .from(context)
            .inflate(
                R.layout.item_pet_grid,
                parent,
                false
            )

        val pet = getItem(position)

        val txtEmoji = view.findViewById<TextView>(R.id.txtEmojiPet)
        val txtNome = view.findViewById<TextView>(R.id.txtNomePet)
        val txtEspecieRaca = view.findViewById<TextView>(R.id.txtEspecieRaca)
        val txtIdade = view.findViewById<TextView>(R.id.txtIdadePet)

        txtEmoji.text = pet.emoji
        txtNome.text = pet.nome
        txtEspecieRaca.text = "${pet.especie} • ${pet.raca}"

        val idade = calcularIdade(pet.anoNascimento)

        txtIdade.text = if (idade == 1) {
            "1 ANO"
        } else {
            "$idade ANOS"
        }

        return view
    }
}
