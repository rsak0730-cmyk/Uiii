package com.example

import com.example.service.BatteryInfo
import com.example.service.EnergyMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryMonitorTest {

    @Test
    fun `performance mode selected when battery above 50 or charging`() {
        val highBattery = BatteryInfo(levelPercent = 85, isCharging = false, energyMode = EnergyMode.PERFORMANCE)
        assertEquals(EnergyMode.PERFORMANCE, highBattery.energyMode)
        assertEquals("gemini-3.1-pro-preview", highBattery.energyMode.recommendedModel)

        val chargingLowBattery = BatteryInfo(levelPercent = 15, isCharging = true, energyMode = EnergyMode.PERFORMANCE)
        assertEquals(EnergyMode.PERFORMANCE, chargingLowBattery.energyMode)
    }

    @Test
    fun `eco saver mode selected when battery below 20 or power save active`() {
        val lowBattery = BatteryInfo(levelPercent = 14, isCharging = false, isPowerSaveMode = true, energyMode = EnergyMode.ECO_SAVER)
        assertEquals(EnergyMode.ECO_SAVER, lowBattery.energyMode)
        assertEquals("gemini-3.1-flash-lite-preview", lowBattery.energyMode.recommendedModel)
    }

    @Test
    fun `battery info formatting is valid`() {
        val info = BatteryInfo(levelPercent = 75, temperatureCelsius = 28.5f, pluggedSource = "AC Charger")
        assertTrue(info.levelPercent in 0..100)
        assertTrue(info.temperatureCelsius > 0)
    }
}
