package com.example.currencyconverter.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    fun format(dateTime: LocalDateTime): String {
        return dateTime.format(formatter)
    }
}