package com.example.currencyconverter.ui.screens

import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.Currency
import com.example.currencyconverter.domain.entity.CurrencyItem
import com.example.currencyconverter.utils.ExchangeRateUtils
import junit.framework.TestCase.assertEquals
import org.junit.Test

class ExchangeRateUtilsTest {

    @Test
    fun `WHEN calculateExchangeRate is right THEN return correct rate`() {

        val mockAccount = Account(Currency.USD, 500.0)


        val rates = listOf(
            CurrencyItem(Currency.USD, "$", "US Dollar", 1.0, mockAccount),
            CurrencyItem(Currency.EUR, "€", "Euro", 0.9, mockAccount),
            CurrencyItem(Currency.RUB, "₽", "Rub", 68.0, mockAccount)
        )

        val rateUsdToEur = ExchangeRateUtils.calculateExchangeRate(rates, "USD", "EUR")
        val rateEurToUsd = ExchangeRateUtils.calculateExchangeRate(rates, "EUR", "USD")
        val rateRubToUsd = ExchangeRateUtils.calculateExchangeRate(rates, "RUB", "USD")
        val rateUsdToRub = ExchangeRateUtils.calculateExchangeRate(rates, "USD", "RUB")
        val rateUnknown =
            ExchangeRateUtils.calculateExchangeRate(rates, "USD", "GBP")

        assertEquals(1.0 / 0.9, rateUsdToEur, DELTA)
        assertEquals(0.9 / 1.0, rateEurToUsd, DELTA)
        assertEquals(68.0 / 1.0, rateRubToUsd, DELTA)
        assertEquals(1.0 / 68.0, rateUsdToRub, DELTA)
        assertEquals(0.0, rateUnknown, DELTA)
    }

    companion object {
        private const val DELTA = 0.0001
    }
}