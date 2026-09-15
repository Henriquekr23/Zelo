package com.example.zelo.model

import java.util.Calendar

/**
 * Camada Model (MVVM) responsável pelos dados dos pets do tutor logado.
 * Mantém a lista de pets e as regras relacionadas a eles.
 */
class PetsModel {

    private val pets = listOf(
        Pet(
            id = 1,
            nome = "Frida",
            especie = "Gata",
            raca = "Persa",
            tutorId = 1,
            anoNascimento = 2023,
            emoji = "🐱"
        ),
        Pet(
            id = 2,
            nome = "Thor",
            especie = "Cão",
            raca = "Golden Retriever",
            tutorId = 1,
            anoNascimento = 2020,
            emoji = "🐶"
        ),
        Pet(
            id = 3,
            nome = "Akira",
            especie = "Cão",
            raca = "Shiba Inu",
            tutorId = 1,
            anoNascimento = 2021,
            emoji = "🐕"
        ),
        Pet(
            id = 4,
            nome = "Nina",
            especie = "Gata",
            raca = "Siamesa",
            tutorId = 1,
            anoNascimento = 2024,
            emoji = "🐈"
        ),
        Pet(
            id = 5,
            nome = "Rex",
            especie = "Cão",
            raca = "Vira-lata",
            tutorId = 1,
            anoNascimento = 2019,
            emoji = "🐩"
        )
    )

    fun listarPetsDoTutor(tutorId: Int): List<Pet> {
        return pets.filter { it.tutorId == tutorId }
    }

    fun buscarPetPorId(petId: Int): Pet? {
        return pets.firstOrNull { it.id == petId }
    }

    /**
     * Função que recebe um parâmetro (ano de nascimento), realiza um
     * processamento (cálculo da diferença de anos) e retorna um valor
     * (a idade estimada do pet em anos).
     */
    fun calcularIdade(anoNascimento: Int): Int {
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val idade = anoAtual - anoNascimento
        return if (idade < 0) 0 else idade
    }
}
