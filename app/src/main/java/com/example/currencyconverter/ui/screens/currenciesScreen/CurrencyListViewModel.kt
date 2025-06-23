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
import com.example.currencyconverter.ui.screens.CurrencyScreenMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
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

            val ratesDeferred = async { getRatesUseCase(INITIAL_CURRENCY_CODE, INITIAL_RATE_VALUE) }
            val accountsDeferred = async { accountUseCase.getAccountsFromRoom().first() }

            val ratesResult = runCatching { ratesDeferred.await() }
            val accounts = accountsDeferred.await()


            ratesResult.onSuccess { currencies ->
                buildSuccessState(
                    currencies = currencies,
                    code = INITIAL_CURRENCY_CODE,
                    rateValue = INITIAL_RATE_VALUE,
                    accounts = accounts
                )
            }.onFailure { error ->
                _currencyListState.value = CurrencyListState.Error
                _uiEvent.emit(UiEvent.ShowSnackbar(error.message.toString()))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startAutoRefresh() {
        if (autoRefreshJob?.isActive == true) return

        autoRefreshJob = viewModelScope.launch {
            _currencyListState
                .filter { it is CurrencyListState.Success || it is CurrencyListState.MoveToTop }
                .flatMapLatest {
                    rates.debounce(1000)
                }
                .collectLatest {
                    val currentState = _currencyListState.value
                    val code = when (currentState) {
                        is CurrencyListState.Success -> currentState.selectedCurrencyCode
                        is CurrencyListState.MoveToTop -> currentState.currency.currencyCode
                        else -> INITIAL_CURRENCY_CODE
                    }

                    val rateValue = when (currentState) {
                        is CurrencyListState.Success -> currentState.rateValue
                        is CurrencyListState.MoveToTop -> INITIAL_RATE_VALUE
                        else -> INITIAL_RATE_VALUE
                    }

                    getRates(code, rateValue)
                }
        }
    }

//    fun startAutoRefresh() {
//        if (autoRefreshJob?.isActive == true) return
//
//        Log.d(TAG, "startAutoRefresh called")
//
//        autoRefreshJob = viewModelScope.launch {
//            rates.debounce(1000)
//                .collectLatest {
//                    val currentState = _currencyListState.value
//                    if (currentState is CurrencyListState.Success ) {
//                        val code = currentState.selectedCurrencyCode
//                        val rateValue = currentState.rateValue
//                        Log.d(TAG, "code $code  rate value $rateValue")
//                        getRates(code, rateValue)
//                    }
//                }
//        }
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
                Log.w(TAG, "Received empty rates list")
            }
        }.onFailure { error ->
            _currencyListState.value = CurrencyListState.Error
            _uiEvent.emit(UiEvent.ShowSnackbar(error.message.toString()))
        }
    }

    private suspend fun buildSuccessState(
        currencies: List<CurrencyItem>,
        code: String,
        rateValue: Double,
        accounts: List<Account> = (_currencyListState.value
                as? CurrencyListState.Success)?.accounts.orEmpty(),
    ) {
        _rates.emit(currencies)

        val uiCurrencies = currencies.map { currency ->
            uiMapper.currencyEntityToCurrencyUi(
                currency,
                isSelected = currency.code.name == code
            )
        }.sortedByDescending { it.isSelected }

        val selectedCurrency = uiCurrencies.firstOrNull { it.currencyCode == code }
            ?: uiCurrencies.first()

        val previousState = _currencyListState.value
        val isSelecting = previousState is CurrencyListState.Success &&
                previousState.selectedCurrencyCode != selectedCurrency.currencyCode

        if (isSelecting) {
            _currencyListState.value = CurrencyListState.MoveToTop(
                currency = selectedCurrency,
                currencies = uiCurrencies,
            )
        } else {
            _currencyListState.value = CurrencyListState.Success(
                currencies = uiCurrencies,
                selectedCurrencyCode = code,
                rateValue = rateValue,
                screenMode = (previousState as? CurrencyListState.Success)?.screenMode
                    ?: CurrencyScreenMode.LIST_MODE,
                accounts = accounts,
            )
        }
    }

    fun selectCurrency(code: String) {
        Log.d(TAG, "select currency")

        val currentState = _currencyListState.value
        if (currentState is CurrencyListState.Success) {
            toMoveToTop(
                currencies = currentState.currencies,
                code = code
            )
        }

        if (currentState is CurrencyListState.MoveToTop) {
            toMoveToTop(
                currencies = currentState.currencies,
                code = code
            )
        }
    }

    private fun toMoveToTop(currencies: List<CurrencyUiModel>, code: String) {
        val currenciesList = currencies.toMutableList()
        val selectedCurrency = currenciesList.find { it.currencyCode == code }


        if (selectedCurrency != null) {
            currenciesList.remove(selectedCurrency)
            currenciesList.add(0, selectedCurrency)

            _currencyListState.value = CurrencyListState.MoveToTop(
                currencies = currenciesList,
                currency = selectedCurrency
            )
        }
    }


    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }


    companion object {
        private const val TAG = "CurrencyListViewModel"
        private const val INITIAL_RATE_VALUE = 1.0
        private const val INITIAL_CURRENCY_CODE = "USD"
    }
}



