package com.example.currencyconverter.ui.screens.transactionsScreen

import com.example.currencyconverter.domain.entity.Transaction

sealed class TransactionState {
    data class Success(val transactions: List<Transaction>) : TransactionState()
    data object Loading : TransactionState()
    data object Error : TransactionState()
    data object Empty : TransactionState()
}