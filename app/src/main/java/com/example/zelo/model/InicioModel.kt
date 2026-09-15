package com.example.zelo.model

class InicioModel {

    private val tutorAtual = Tutor(
        id = 1,
        nome = "Camila",
        telefone = "(11) 98421-7730",
        email = "camila@email.com"
    )

    val todosPets = listOf(
        Pet(
            id = 1,
            nome = "Frida",
            especie = "Gata",
            raca = "Persa",
            tutorId = 1,
            anoNascimento = 2023,
            emoji = "🐾"
        ),
        Pet(
            id = 2,
            nome = "Thor",
            especie = "Cão",
            raca = "Golden Retriever",
            tutorId = 1,
            anoNascimento = 2019,
            emoji = "🐶"
        )
    )

    private val procedimentos = listOf(
        Procedimento(
            id = 1,
            petId = 1,
            tipo = TipoProcedimento.VACINA,
            descricao = "Vacina antirrábica",
            data = "15/09/2026"
        ),
        Procedimento(
            id = 2,
            petId = 1,
            tipo = TipoProcedimento.VERMIFUGO,
            descricao = "Vermífugo trimestral",
            data = "04/10/2026"
        ),
        Procedimento(
            id = 3,
            petId = 1,
            tipo = TipoProcedimento.CONSULTA,
            descricao = "Check-up de rotina",
            data = "02/08/2026"
        ),
        Procedimento(
            id = 4,
            petId = 2,
            tipo = TipoProcedimento.VACINA,
            descricao = "Vacina V10 (Anual)",
            data = "22/09/2026"
        ),
        Procedimento(
            id = 5,
            petId = 2,
            tipo = TipoProcedimento.VERMIFUGO,
            descricao = "Vermífugo Plus",
            data = "10/10/2026"
        )
    )

    fun buscarTutorAtual(): Tutor {
        return tutorAtual
    }

    fun buscarPetAtivo(): Pet {
        return todosPets.first()
    }

    fun buscarProximosCuidados(
        petId: Int,
        limite: Int
    ): List<Procedimento> {
        return procedimentos
            .filter { it.petId == petId }
            .take(limite)
    }

    fun buscarProximoLembrete(petId: Int): Procedimento? {
        return buscarProximosCuidados(
            petId = petId,
            limite = 1
        ).firstOrNull()
    }
}