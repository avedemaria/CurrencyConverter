package com.example.currencyconverter.ui.screens.currenciesEditScreen

sealed class UiEditEvent {
    data class ShowSnackbar(val message: String) : UiEditEvent()
}