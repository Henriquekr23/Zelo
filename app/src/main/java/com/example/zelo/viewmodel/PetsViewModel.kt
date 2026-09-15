package com.example.zelo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.zelo.model.Pet
import com.example.zelo.model.PetsModel

class PetsViewModel : ViewModel() {

    private val model = PetsModel()

    private val _pets = MutableLiveData<List<Pet>>()

    val pets: LiveData<List<Pet>>
        get() = _pets

    init {
        carregarPets()
    }

    fun carregarPets() {
        _pets.value = model.listarPetsDoTutor(tutorId = 1)
    }

    fun calcularIdade(anoNascimento: Int): Int {
        return model.calcularIdade(anoNascimento)
    }
}
