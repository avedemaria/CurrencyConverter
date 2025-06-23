package com.example.currencyconverter.ui.screens.transactionsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.domain.entity.Transaction
import com.example.currencyconverter.domain.usecases.TransactionUseCase
import com.example.currencyconverter.ui.screens.currenciesScreen.UiEvent
import com.example.currencyconverter.ui.screens.exchangeScreen.ExchangeState
import com.example.currencyconverter.ui.screens.exchangeScreen.UiExchangeEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionUseCase: TransactionUseCase,

    ) : ViewModel() {

    private val _transactionState = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val transactionState: StateFlow<TransactionState> get() = _transactionState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiTransactionEvent>()
    val uiEvent: SharedFlow<UiTransactionEvent> get() = _uiEvent.asSharedFlow()


    init {
        loadTransactions()
    }


    fun loadTransactions() {
        viewModelScope.launch {
            try {
                transactionUseCase.getTransactions().let { transactions ->
                    _transactionState.value = when {
                        transactions.isNotEmpty() -> TransactionState.Success(transactions)
                        else -> TransactionState.Empty
                    }
                }
            } catch (e: Exception) {
                _transactionState.value = TransactionState.Error
                _uiEvent.emit(UiTransactionEvent.ShowSnackbar(e.message.toString()))
            }

        }
    }
}