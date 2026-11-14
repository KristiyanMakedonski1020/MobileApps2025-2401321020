package com.mobileapp2025.expensebudget.domain

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class DateConverterTest {

    private val date = LocalDate.of(2026, 11, 12)
    private val pickerValue = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    @Test
    fun `picker value becomes start of day in Sofia`() {
        val zone = ZoneId.of("Europe/Sofia")
        val expected = date.atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, DateConverter.utcPickerToLocalStartOfDay(pickerValue, zone))
    }

    @Test
    fun `picker value keeps the same day west of UTC`() {
        val zone = ZoneId.of("America/Los_Angeles")
        val expected = date.atStartOfDay(zone).toInstant().toEpochMilli()
        assertEquals(expected, DateConverter.utcPickerToLocalStartOfDay(pickerValue, zone))
    }

    @Test
    fun `round trip returns the picker value`() {
        val zone = ZoneId.of("Europe/Sofia")
        val stored = DateConverter.utcPickerToLocalStartOfDay(pickerValue, zone)
        assertEquals(pickerValue, DateConverter.localStartOfDayToUtcPicker(stored, zone))
    }

    @Test
    fun `start of today is at midnight`() {
        val zone = ZoneId.of("Europe/Sofia")
        val today = DateConverter.startOfToday(zone)
        assertEquals(today, DateConverter.utcPickerToLocalStartOfDay(
            DateConverter.localStartOfDayToUtcPicker(today, zone), zone))
    }
}