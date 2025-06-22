package com.example.currencyconverter.ui.screens.transactionsScreen

sealed class UiTransactionEvent {
    data class ShowSnackbar(val message: String) : UiTransactionEvent()

}