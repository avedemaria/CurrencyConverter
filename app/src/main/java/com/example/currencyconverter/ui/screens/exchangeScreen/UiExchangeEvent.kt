package com.example.currencyconverter.ui.screens.exchangeScreen

sealed class UiExchangeEvent {
    data class ShowSnackbar(val message: String) : UiExchangeEvent()
    data object NavigateToCurrencyList : UiExchangeEvent()
}