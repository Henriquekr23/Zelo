package com.example.zelo.model

class HistoricoModel {

    private val petAtivo = Pet(
        id = 1,
        nome = "Frida",
        especie = "Gata",
        raca = "Persa",
        tutorId = 1
    )

    private val historico = listOf(
        Procedimento(
            id = 3,
            petId = 1,
            tipo = TipoProcedimento.CONSULTA,
            descricao = "Check-up de rotina",
            data = "02/08/2026"
        ),
        Procedimento(
            id = 4,
            petId = 1,
            tipo = TipoProcedimento.VACINA,
            descricao = "Vacina polivalente felina",
            data = "15/07/2026"
        ),
        Procedimento(
            id = 5,
            petId = 1,
            tipo = TipoProcedimento.VERMIFUGO,
            descricao = "Vermífugo trimestral",
            data = "04/07/2026"
        )
    )

    fun buscarPetAtivo(): Pet {
        return petAtivo
    }

    fun buscarHistorico(petId: Int): List<Procedimento> {
        return historico.filter { it.petId == petId }
    }
}
