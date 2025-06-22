package com.example.currencyconverter.data.dataSource.remote.dto

import com.squareup.moshi.Json

data class CurrencyInfo(
    @Json(name = "symbol_native") val symbol: String,
    @Json(name = "name") val fullName: String,
) {
}