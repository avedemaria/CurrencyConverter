package com.example.currencyconverter.ui.screens.exchangeScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.Transaction
import com.example.currencyconverter.domain.usecases.GetRatesUseCase
import com.example.currencyconverter.domain.usecases.TransactionUseCase
import com.example.currencyconverter.utils.ExchangeRateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject


@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val transactionUseCase: TransactionUseCase,
    private val getRatesUseCase: GetRatesUseCase

    ) : ViewModel() {

    private val _exchangeState = MutableStateFlow<ExchangeState>(ExchangeState.Loading)
    val exchangeState: StateFlow<ExchangeState> get() = _exchangeState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiExchangeEvent>()
    val uiEvent: SharedFlow<UiExchangeEvent> get() = _uiEvent.asSharedFlow()


   fun loadExchangeRate(
        baseCurrencyCode: String,
        targetCurrencyCode: String,
        rateValue: Double = 1.0,
    ) {
        viewModelScope.launch {
            _exchangeState.value = ExchangeState.Loading
            try {
                val rates = getRatesUseCase.invoke(baseCurrencyCode, rateValue)
                val result = ExchangeRateUtils.calculateExchangeRate(rates, baseCurrencyCode,
                    targetCurrencyCode)
                _exchangeState.value = ExchangeState.Success(result)
            } catch (e: Exception) {
                _exchangeState.value = ExchangeState.Error
                _uiEvent.emit(UiExchangeEvent.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }


    fun saveTransaction(
        fromAccount: Account,
        toAccount: Account,
        fromAmount: Double,
        toAmount: Double,
    ) {

        viewModelScope.launch {
            runCatching {
                val transaction = Transaction(
                    id = 0,
                    from = fromAccount.code.name,
                    to = toAccount.code.name,
                    fromAmount = fromAmount,
                    toAmount = toAmount,
                    dateTime = LocalDateTime.now()
                )

                val updatedFromAccount =
                    fromAccount.copy(balance = fromAccount.balance - fromAmount)
                val updatedToAccount = toAccount.copy(balance = toAccount.balance + toAmount)

                transactionUseCase.saveTransaction(
                    transaction = transaction,
                    updatedFrom = updatedFromAccount,
                    updatedTo = updatedToAccount
                )

                _uiEvent.emit(UiExchangeEvent.NavigateToCurrencyList)

            }.onFailure { error ->
                _exchangeState.value = ExchangeState.Error
                _uiEvent.emit(UiExchangeEvent.ShowSnackbar("Error: ${error.message}"))
            }
        }
    }

}