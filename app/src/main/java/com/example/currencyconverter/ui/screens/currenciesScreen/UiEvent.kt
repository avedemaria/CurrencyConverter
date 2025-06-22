package com.example.currencyconverter.ui.screens.currenciesScreen

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}