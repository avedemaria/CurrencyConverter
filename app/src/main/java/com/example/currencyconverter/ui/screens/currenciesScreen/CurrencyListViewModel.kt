package com.example.currencyconverter.ui.screens.currenciesScreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.CurrencyItem
import com.example.currencyconverter.domain.usecases.AccountUseCase
import com.example.currencyconverter.domain.usecases.CurrencyInfoUseCase
import com.example.currencyconverter.domain.usecases.GetRatesUseCase
import com.example.currencyconverter.ui.CurrencyUiModel
import com.example.currencyconverter.ui.mapper.CurrencyUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    private val getRatesUseCase: GetRatesUseCase,
    private val currencyInfoUseCase: CurrencyInfoUseCase,
    private val accountUseCase: AccountUseCase,
    private val uiMapper: CurrencyUiMapper,

    ) : ViewModel() {

    private val _currencyListState = MutableStateFlow<CurrencyListState>(CurrencyListState.Loading)
    val currencyListState: StateFlow<CurrencyListState> get() = _currencyListState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> get() = _uiEvent.asSharedFlow()

    private val _rates = MutableStateFlow<List<CurrencyItem>>(emptyList())
    private val rates = _rates.asStateFlow()

    private var autoRefreshJob: Job? = null


    init {

        viewModelScope.launch {

            _currencyListState.value = CurrencyListState.Loading

            currencyInfoUseCase()
            Log.d("ViewModel", "Currency info loaded")

            val ratesDeferred = async { getRatesUseCase(INITIAL_CURRENCY_CODE, INITIAL_AMOUNT) }
            val accountsDeferred = async { accountUseCase.getAccountsFromRoom().first() }

            val ratesResult = runCatching { ratesDeferred.await() }
            val accounts = accountsDeferred.await()
            Log.d("ViewModel", "Accounts loaded: ${accounts.size}")

            ratesResult.onSuccess { currencies ->
                buildSuccessState(
                    currencies = currencies,
                    code = INITIAL_CURRENCY_CODE,
                    amount = INITIAL_AMOUNT,
                    accounts = accounts
                )
            }.onFailure { error ->
                _currencyListState.value = CurrencyListState.Error
                _uiEvent.emit(UiEvent.ShowSnackbar(error.message.toString()))
            }
        }
    }


    fun startAutoRefresh() {
        if (autoRefreshJob?.isActive == true) return

        Log.d("ViewModel", "startAutoRefresh called")

        autoRefreshJob = viewModelScope.launch {
            rates.debounce(1000)
                .collectLatest {
                    val currentState = _currencyListState.value
                    if (currentState is CurrencyListState.Success) {
                        val code = currentState.selectedCurrencyCode
                        val amount = currentState.enteredAmount
                        getRates(code, amount)
                    }
                }
        }
    }


//
//    private fun filterCurrenciesByMode(currencies: List<CurrencyUiModel>): List<CurrencyUiModel> {
//        val currentState = _currencyListState.value
//        if (currentState !is CurrencyListState.Success) return currencies
//
//        return when (currentState.screenMode) {
//            CurrencyScreenMode.LIST_MODE -> currencies
//            CurrencyScreenMode.INPUT_MODE -> {
//                currencies.filter { currency ->
//                    val balanceAvailable = currency.balance ?: 0.0
//                    val neededAmount = calculateNeededAmount(currency, currentState.enteredAmount)
//                    balanceAvailable >= neededAmount
//                }
//            }
//        }
//    }
//
//
//    private fun calculateNeededAmount(currency: CurrencyUiModel, enteredAmount: Double): Double {
//        return enteredAmount * currency.amount
//    }


    private suspend fun getRates(
        code: String,
        amount: Double,
    ) {
        runCatching {
            getRatesUseCase(code, amount)
        }.onSuccess { currencies ->
            if (currencies.isNotEmpty()) {
                buildSuccessState(currencies, code, amount)
            } else {
                Log.w("ViewModel", "Received empty rates list")
            }
        }.onFailure { error ->
            _currencyListState.value = CurrencyListState.Error
            _uiEvent.emit(UiEvent.ShowSnackbar(error.message.toString()))
        }
    }

    private suspend fun buildSuccessState(
        currencies: List<CurrencyItem>,
        code: String,
        amount: Double,
        accounts: List<Account> = (_currencyListState.value
                as? CurrencyListState.Success)?.accounts.orEmpty(),
    ) {
        _rates.emit(currencies)

        Log.d("ViewModel", "Currencies before mapping: ${currencies.size}")
        val uiCurrencies = currencies.map { currency ->
            uiMapper.currencyEntityToCurrencyUi(
                currency,
                isSelected = currency.code.name == code
            )
        }.sortedByDescending { it.isSelected }
        Log.d("ViewModel", "Currencies after mapping: ${uiCurrencies.size}")

        _currencyListState.value = CurrencyListState.Success(
            currencies = uiCurrencies,
            selectedCurrencyCode = code,
            enteredAmount = amount,
            screenMode = CurrencyScreenMode.LIST_MODE,
            accounts = accounts
        )
    }

    fun selectCurrency(code: String) {
        Log.d("ViewModel", "select currency")
        val currentState = _currencyListState.value
        if (currentState is CurrencyListState.Success) {
            _currencyListState.value = currentState.copy(selectedCurrencyCode = code)
            Log.d("ViewModel", "selected code: $code")
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }


    companion object {
        private const val INITIAL_AMOUNT = 1.0
        private const val INITIAL_CURRENCY_CODE = "USD"
    }
}



