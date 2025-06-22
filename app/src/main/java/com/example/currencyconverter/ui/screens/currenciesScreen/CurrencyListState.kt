package com.example.currencyconverter.ui.screens.currenciesScreen

import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.ui.CurrencyUiModel

sealed class CurrencyListState {
    data class Success(
        val currencies: List<CurrencyUiModel>,
        val selectedCurrencyCode: String,
        val enteredAmount: Double,
        val screenMode: CurrencyScreenMode,
        val accounts: List<Account> = emptyList(),
    ) : CurrencyListState()

    data object Loading : CurrencyListState()
    data object Error : CurrencyListState()
}