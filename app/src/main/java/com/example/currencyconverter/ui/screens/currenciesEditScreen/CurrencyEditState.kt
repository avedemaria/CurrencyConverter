package com.example.currencyconverter.ui.screens.currenciesEditScreen

import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.ui.CurrencyUiModel

sealed class CurrencyEditState {
    data class Success(
        val currencies: List<CurrencyUiModel>,
        val accounts: List<Account>,
        val enteredAmount: Double,
        val selectedCurrencyCode: String,
    ) : CurrencyEditState()

    data object Loading : CurrencyEditState()
    data object Error : CurrencyEditState()
}



