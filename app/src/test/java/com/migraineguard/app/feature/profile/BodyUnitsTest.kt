package com.migraineguard.app.feature.profile

import org.junit.Assert.*
import org.junit.Test

class BodyUnitsTest {
    @Test fun savedHeightDeterminesImperialFields() {
        assertEquals(6 to 2, imperialHeight(188f))
        assertEquals(5 to 7, imperialHeight(170f))
    }
    @Test fun weightUnitSwitchPreservesThePhysicalWeight() {
        val pounds = convertedWeight(70f, true, false)
        assertEquals(154.32358f, pounds, 0.001f)
        assertEquals(70f, convertedWeight(pounds, false, true), 0.001f)
        assertEquals(70f, convertedWeight(70f, true, true), 0f)
    }
}
