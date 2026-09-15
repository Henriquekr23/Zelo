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

    private lateinit var binding: ActivityMeusPetsBinding

    private val viewModel: PetsViewModel by viewModels()

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

            binding.gridViewPets.setOnItemClickListener { _, _, posicao, _ ->
                val petSelecionado = pets[posicao]
                abrirAgendamentoParaPet(petSelecionado)
            }
        }
    }

    private fun configurarEventos() {
        binding.btnVoltarMeusPets.setOnClickListener {
            finish()
        }
    }

    /**
     * Envia o pet clicado no GridView para a tela de novo agendamento
     * através de extras no Intent — o nome do pet chega pré-preenchido
     * na tela de destino (NovoAgendamentoActivity).
     */
    private fun abrirAgendamentoParaPet(pet: Pet) {
        Toast.makeText(
            this,
            "${pet.nome} tem ${viewModel.calcularIdade(pet.anoNascimento)} anos",
            Toast.LENGTH_SHORT
        ).show()

        val intent = Intent(this, NovoAgendamentoActivity::class.java)
        intent.putExtra("NOME_PET_SELECIONADO", pet.nome)
        startActivity(intent)
    }

    private fun configurarBarraNavegacao() {
        binding.composeViewBottomNavPets.setContent {
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
}
