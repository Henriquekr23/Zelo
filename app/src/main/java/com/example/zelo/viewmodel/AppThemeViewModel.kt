package com.example.zelo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

import com.example.zelo.data.repository.AppColorTheme
import com.example.zelo.data.repository.AppThemeRepository

import kotlinx.coroutines.flow.StateFlow

class AppThemeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = AppThemeRepository(application)

    val tema: StateFlow<AppColorTheme> = repository.tema

    fun selecionarTema(tema: AppColorTheme) {
        repository.alterarTema(tema)
    }
}