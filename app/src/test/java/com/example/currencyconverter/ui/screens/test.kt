package com.example.currencyconverter.ui.screens

fun main() {
    fun generateFlagMapCode(): String {
        val currencies = listOf(
            "AUD", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP",
            "HKD", "HRK", "HUF", "IDR", "ILS", "INR", "ISK", "JPY", "KRW", "MXN",
            "MYR", "NOK", "NZD", "PHP", "PLN", "RON", "RUB", "SEK", "SGD", "THB",
            "TRY", "USD", "ZAR"
        )

        return currencies.joinToString(",\n") { currency ->
            "Currency.$currency to R.drawable.${currency.lowercase()}_static"
        }
    }
    println(generateFlagMapCode())
}