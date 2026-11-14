package com.mobileapp2025.expensebudget.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Converts between the dates stored in the database (start of the day in the device time zone)
 * and the values used by MaterialDatePicker (start of the day in UTC).
 */
object DateConverter {

    /** Start of today in [zone], as epoch millis. */
    fun startOfToday(zone: ZoneId = ZoneId.systemDefault()): Long =
        LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()

    /** Picker value (UTC midnight of the chosen day) to the stored value. */
    fun utcPickerToLocalStartOfDay(pickerMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(pickerMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

    /** Stored value to the value the picker expects as its initial selection. */
    fun localStartOfDayToUtcPicker(storedMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(storedMillis)
            .atZone(zone)
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
}