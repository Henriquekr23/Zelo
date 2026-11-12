package com.example.zelo.viewmodel

import android.app.Application
import com.example.zelo.data.local.AgendamentoDao
import com.example.zelo.data.local.AgendamentoEntity
import com.example.zelo.data.local.PetDao
import com.example.zelo.data.local.PetEntity
import com.example.zelo.data.local.ZeloDatabase
import com.example.zelo.model.StatusAgendamento
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import androidx.lifecycle.viewModelScope
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

/** Exercise the real ViewModel/repositories without opening any Room database. */
@OptIn(ExperimentalCoroutinesApi::class)
class HistoricoViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val application = mockk<Application>()
    private val database = mockk<ZeloDatabase>()
    private val petDao = mockk<PetDao>()
    private val appointmentDao = mockk<AgendamentoDao>()
    private val pets = MutableStateFlow(listOf(
        PetEntity(id = 1, nome = "Frida", especie = "Gata", raca = "Persa", tutorId = 1),
        PetEntity(id = 2, nome = "Thor", especie = "Cachorro", raca = "SRD", tutorId = 1)
    ))
    private val appointments = MutableStateFlow(listOf(
        appointment(1, "Vacina antirrábica", "15/09/2026"),
        appointment(2, "Vermífugo trimestral", "04/10/2026"),
        appointment(3, "Consulta de retorno", "20/10/2026"),
        appointment(4, "Consulta de Thor", "21/10/2026", petId = 2),
        appointment(5, "Consulta futura", "22/10/2026", status = StatusAgendamento.AGENDADO),
        appointment(6, "Consulta cancelada", "23/10/2026", status = StatusAgendamento.CANCELADO)
    ))
    private lateinit var model: HistoricoViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkObject(ZeloDatabase.Companion)
        every { ZeloDatabase.getInstance(application) } returns database
        every { database.petDao() } returns petDao
        every { database.agendamentoDao() } returns appointmentDao
        every { petDao.observarPorTutor(1) } returns pets
        every { appointmentDao.observarTodos() } returns appointments
        model = HistoricoViewModel(application)
    }

    @After fun tearDown() {
        model.viewModelScope.cancel()
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test fun estadoCarregadoContemFridaComTresAtendimentosEConsultaPrimeiro() = runTest(dispatcher) {
        assertTrue(model.uiState.value.carregando)
        assertNull(model.uiState.value.petAtivo)
        runCurrent()
        val state = model.uiState.value
        assertFalse(state.carregando)
        assertEquals("Frida", requireNotNull(state.petAtivo).nome)
        assertEquals(3, state.historico.size)
        assertEquals("Consulta de retorno", state.historico.first().descricao)
    }

    @Test fun historicoContemConsultaVacinaEVermifugo() = runTest(dispatcher) {
        runCurrent()
        assertEquals(
            listOf("Consulta de retorno", "Vermífugo trimestral", "Vacina antirrábica"),
            model.uiState.value.historico.map { it.descricao }
        )
    }

    @Test fun atendimentosPertencemAoPetAtivoGataPersaESaoConcluidos() = runTest(dispatcher) {
        runCurrent()
        val state = model.uiState.value
        val pet = requireNotNull(state.petAtivo)
        assertEquals("Gata", pet.especie)
        assertEquals("Persa", pet.raca)
        assertTrue(state.historico.isNotEmpty())
        assertTrue(state.historico.all { it.petId == pet.id && it.status == StatusAgendamento.CONCLUIDO })
    }

    @Test fun historicoTemDescricoesEDatasDoMaisRecenteAoMaisAntigo() = runTest(dispatcher) {
        runCurrent()
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT).apply { isLenient = false }
        val dates = model.uiState.value.historico.map {
            assertTrue(it.descricao.isNotBlank())
            val date = requireNotNull(format.parse(it.data))
            assertEquals(it.data, format.format(date))
            date
        }
        assertTrue(dates.size > 1)
        assertTrue(dates.zipWithNext().all { (previous, next) -> previous >= next })
    }

    @Test fun selecionarOutroPetFiltraHistoricoSemMisturarAtendimentos() = runTest(dispatcher) {
        runCurrent()
        model.selecionarPet(2)
        assertEquals("Thor", requireNotNull(model.uiState.value.petAtivo).nome)
        assertEquals(listOf(4), model.uiState.value.historico.map { it.id })
        model.selecionarPet(999)
        assertEquals(2, model.uiState.value.petAtivo?.id)
    }

    @Test fun novasEmissoesAtualizamHistoricoEOrdenamHorarioEAno() = runTest(dispatcher) {
        runCurrent()
        appointments.value += listOf(
            appointment(7, "Retorno à tarde", "20/10/2026", time = "15:00"),
            appointment(8, "Cuidado em janeiro", "01/01/2027")
        )
        runCurrent()
        assertEquals(listOf(8, 7, 3, 2, 1), model.uiState.value.historico.map { it.id })
    }

    @Test fun listaVaziaTerminaCarregamentoSemPetNemHistorico() = runTest(dispatcher) {
        pets.value = emptyList()
        appointments.value = emptyList()
        runCurrent()
        assertFalse(model.uiState.value.carregando)
        assertNull(model.uiState.value.petAtivo)
        assertTrue(model.uiState.value.historico.isEmpty())
    }

    private fun appointment(
        id: Int, description: String, date: String, petId: Int = 1,
        time: String = "10:00", status: StatusAgendamento = StatusAgendamento.CONCLUIDO
    ) = AgendamentoEntity(
        id = id, petId = petId, nomePet = if (petId == 1) "Frida" else "Thor",
        data = date, horario = time, descricao = description, status = status.name
    )
}
