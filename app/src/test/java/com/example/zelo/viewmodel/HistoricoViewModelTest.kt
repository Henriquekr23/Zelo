package com.example.zelo.viewmodel

import com.example.zelo.model.TipoProcedimento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class HistoricoViewModelTest {

    @Test
    fun estadoInicialContemFridaComTresProcedimentosEConsultaPrimeiro() {
        val uiState = HistoricoViewModel().uiState.value

        assertEquals("Frida", uiState.petAtivo.nome)
        assertEquals(3, uiState.historico.size)
        assertEquals(TipoProcedimento.CONSULTA, uiState.historico.first().tipo)
    }

    @Test
    fun historicoContemConsultaVacinaEVermifugo() {
        val historico = HistoricoViewModel().uiState.value.historico

        assertEquals(
            listOf(
                TipoProcedimento.CONSULTA,
                TipoProcedimento.VACINA,
                TipoProcedimento.VERMIFUGO
            ),
            historico.map { it.tipo }
        )
    }

    @Test
    fun procedimentosPertencemAoPetAtivoGataPersa() {
        val uiState = HistoricoViewModel().uiState.value

        assertEquals("Gata", uiState.petAtivo.especie)
        assertEquals("Persa", uiState.petAtivo.raca)
        assertTrue(uiState.historico.isNotEmpty())
        assertTrue(uiState.historico.all { it.petId == uiState.petAtivo.id })
    }

    @Test
    fun historicoTemDescricoesEDatasDoMaisRecenteAoMaisAntigo() {
        val historico = HistoricoViewModel().uiState.value.historico
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply {
            isLenient = false
        }
        val datas = historico.map { procedimento ->
            assertTrue(procedimento.descricao.isNotBlank())
            val data = requireNotNull(formato.parse(procedimento.data))
            assertEquals(procedimento.data, formato.format(data))
            data
        }

        assertTrue(datas.size > 1)
        assertTrue(datas.zipWithNext().all { (anterior, seguinte) -> anterior >= seguinte })
    }
}
