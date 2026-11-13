package com.example.zelo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/** A presentation toggle only. Does not load pets, preferences, maps or location services. */
class ClinicDemoViewModel : ViewModel() {
    var routeVisible by mutableStateOf(false)
        private set
    fun toggleRoute() { routeVisible = !routeVisible }
}
