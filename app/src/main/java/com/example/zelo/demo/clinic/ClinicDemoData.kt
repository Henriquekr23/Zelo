package com.example.zelo.demo.clinic

/** Diagram positions in a unit rectangle, never latitude/longitude or GPS coordinates. */
data class MapPoint(val x: Float, val y: Float)

object ClinicDemoData {
    const val NAME = "Clínica Jardim Veterinária"
    const val MAP_NOTICE = "Mapa ilustrativo — localização fictícia."
    const val ADDRESS = "Rua das Patinhas, 120 • Bairro Jardim dos Pets"
    const val CITY = "Cidade ilustrativa • endereço inteiramente fictício"
    const val HOURS = "Segunda a sexta: 08h às 18h\nSábado: 08h às 12h\nHorários demonstrativos"
    const val DISTANCE = "1,2 km"
    const val DURATION = "4 min"
    val services = listOf("Consultas veterinárias", "Vacinação e prevenção", "Exames de rotina", "Orientação de cuidados")
    val origin = MapPoint(0.16f, 0.82f)
    val destination = MapPoint(0.76f, 0.30f)
    val route = listOf(origin, MapPoint(0.38f, 0.82f), MapPoint(0.38f, 0.51f),
        MapPoint(0.76f, 0.51f), destination)
}
