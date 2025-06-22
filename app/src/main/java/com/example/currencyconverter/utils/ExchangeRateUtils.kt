package com.example.currencyconverter.utils

import com.example.currencyconverter.domain.entity.CurrencyItem

object ExchangeRateUtils {
    fun calculateExchangeRate(
        rates: List<CurrencyItem>,
        fromCurrencyCode: String,
        toCurrencyCode: String
    ): Double {
        val fromRate = rates.find { it.code.name == fromCurrencyCode }?.rateValue ?: return 0.0
        val toRate = rates.find { it.code.name == toCurrencyCode }?.rateValue ?: return 0.0
        return fromRate / toRate
    }
}
