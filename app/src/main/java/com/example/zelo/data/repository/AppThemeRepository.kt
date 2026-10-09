package com.example.zelo.data.repository

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class AppColorTheme {
    ORIGINAL,
    VERDE,
    AZUL
}

class AppThemeRepository(context: Context) {

    private val preferences =
        context.applicationContext.getSharedPreferences(
            "zelo_preferencias",
            Context.MODE_PRIVATE
        )

    private val temaSalvo = preferences.getString(
        "tema_cor",
        AppColorTheme.ORIGINAL.name
    )

    private val _tema = MutableStateFlow(
        AppColorTheme.entries.firstOrNull {
            it.name == temaSalvo
        } ?: AppColorTheme.ORIGINAL
    )

    val tema: StateFlow<AppColorTheme> = _tema

    fun alterarTema(novoTema: AppColorTheme) {

        preferences.edit()
            .putString("tema_cor", novoTema.name)
            .apply()

        _tema.value = novoTema
    }
}