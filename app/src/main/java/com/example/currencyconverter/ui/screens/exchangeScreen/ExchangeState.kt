package com.example.currencyconverter.ui.screens.exchangeScreen

sealed class ExchangeState {
    data class Success(val exchangeRate: Double) : ExchangeState()
    data object Loading : ExchangeState()
    data object Error : ExchangeState()
}