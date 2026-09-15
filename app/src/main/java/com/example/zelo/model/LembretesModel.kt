package com.example.zelo.model

class LembretesModel {

    private val petAtivo = Pet(
        id = 1,
        nome = "Frida",
        especie = "Gata",
        raca = "Persa",
        tutorId = 1
    )

    private val vencimentos = listOf(
        Procedimento(
            id = 10,
            petId = 1,
            tipo = TipoProcedimento.VACINA,
            descricao = "Vacina antirrábica",
            data = "20/09/2026"
        ),
        Procedimento(
            id = 11,
            petId = 1,
            tipo = TipoProcedimento.VERMIFUGO,
            descricao = "Vermífugo trimestral",
            data = "04/10/2026"
        ),
        Procedimento(
            id = 12,
            petId = 1,
            tipo = TipoProcedimento.CONSULTA,
            descricao = "Consulta de retorno",
            data = "20/10/2026"
        )
    )

    fun buscarPetAtivo(): Pet {
        return petAtivo
    }

    fun buscarProximosVencimentos(
        petId: Int
    ): List<Procedimento> {
        return vencimentos.filter {
            it.petId == petId
        }
    }
}