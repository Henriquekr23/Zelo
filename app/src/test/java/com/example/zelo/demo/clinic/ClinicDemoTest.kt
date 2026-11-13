package com.example.zelo.demo.clinic

import com.example.zelo.data.local.ZeloDatabase
import com.example.zelo.viewmodel.ClinicDemoViewModel
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test

class ClinicDemoTest {
    @Test fun routeFollowsIllustratedStreetsAndConnectsFictitiousPoints() {
        val route = ClinicDemoData.route
        assertEquals(ClinicDemoData.origin, route.first())
        assertEquals(ClinicDemoData.destination, route.last())
        assertTrue(route.all { it.x in 0f..1f && it.y in 0f..1f })
        assertTrue(route.zipWithNext().all { (a, b) ->
            a != b && (a.x == b.x || a.y == b.y)
        })
    }

    @Test fun routeCanBeRepeatedAndNewSessionsStartWithoutRoute() {
        val model = ClinicDemoViewModel()
        assertFalse(model.routeVisible)
        model.toggleRoute()
        assertTrue(model.routeVisible)
        model.toggleRoute()
        assertFalse(model.routeVisible)
        model.toggleRoute()
        assertFalse(ClinicDemoViewModel().routeVisible)
        assertTrue(model.routeVisible)
    }

    @Test fun clinicPresentationNeverOpensRoom() {
        mockkObject(ZeloDatabase.Companion)
        try {
            every { ZeloDatabase.getInstance(any()) } throws AssertionError("Clinic demo must not open Room")
            val model = ClinicDemoViewModel()
            repeat(4) { model.toggleRoute() }
            assertTrue(ClinicDemoData.services.isNotEmpty())
            assertTrue(ClinicDemoData.MAP_NOTICE.contains("fictícia"))
            verify(exactly = 0) { ZeloDatabase.getInstance(any()) }
        } finally { unmockkObject(ZeloDatabase.Companion) }
    }
}
