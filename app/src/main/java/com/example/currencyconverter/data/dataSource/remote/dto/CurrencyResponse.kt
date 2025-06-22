package com.example.currencyconverter.data.dataSource.remote.dto

data class CurrencyResponse(
    val data: Map<String, CurrencyInfo>,
) {
}