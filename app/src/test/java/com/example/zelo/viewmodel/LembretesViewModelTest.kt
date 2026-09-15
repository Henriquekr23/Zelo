package com.example.zelo.viewmodel

import com.example.zelo.model.TipoProcedimento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LembretesViewModelTest {

    @Test
    fun carregaPetAtivoCorretamente() {
        val viewModel = LembretesViewModel()
        val estado = viewModel.uiState.value

        assertEquals("Frida", estado.pet.nome)
        assertEquals("Gata", estado.pet.especie)
        assertEquals("Persa", estado.pet.raca)
    }

    @Test
    fun carregaTresProximosVencimentos() {
        val viewModel = LembretesViewModel()

        assertEquals(
            3,
            viewModel.uiState.value.vencimentos.size
        )
    }

    @Test
    fun possuiVacinaVermifugoEConsulta() {
        val tipos = LembretesViewModel()
            .uiState
            .value
            .vencimentos
            .map { it.tipo }

        assertTrue(TipoProcedimento.VACINA in tipos)
        assertTrue(TipoProcedimento.VERMIFUGO in tipos)
        assertTrue(TipoProcedimento.CONSULTA in tipos)
    }

    @Test
    fun vencimentosPertencemAoPetAtivo() {
        val estado = LembretesViewModel()
            .uiState
            .value

        assertTrue(
            estado.vencimentos.all {
                it.petId == estado.pet.id
            }
        )
    }
}