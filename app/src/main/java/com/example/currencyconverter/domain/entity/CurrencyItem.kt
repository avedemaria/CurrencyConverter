package com.example.currencyconverter.domain.entity


data class CurrencyItem(
    val code: Currency,
    val symbol: String,
    val name: String,
    val rateValue: Double,
    val account: Account
)