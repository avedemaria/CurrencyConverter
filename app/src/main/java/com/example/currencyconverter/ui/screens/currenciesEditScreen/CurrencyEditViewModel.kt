package com.example.currencyconverter.ui.screens.currenciesEditScreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.usecases.AccountUseCase
import com.example.currencyconverter.domain.usecases.GetRatesUseCase
import com.example.currencyconverter.ui.CurrencyUiModel
import com.example.currencyconverter.ui.mapper.CurrencyUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CurrencyEditViewModel @Inject constructor(
    private val getRatesUseCase: GetRatesUseCase,
    private val accountUseCase: AccountUseCase,
    private val uiMapper: CurrencyUiMapper,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _currencyEditState = MutableStateFlow<CurrencyEditState>(CurrencyEditState.Loading)
    val currencyListState: StateFlow<CurrencyEditState> get() = _currencyEditState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEditEvent>()
    val uiEvent: SharedFlow<UiEditEvent> get() = _uiEvent.asSharedFlow()



    private var selectedCurrency: CurrencyUiModel =
        savedStateHandle.get<CurrencyUiModel>(CURRENCY_KEY)
            ?: throw IllegalArgumentException("No currency passed")


    init {
            loadInitialData()
    }


    fun loadInitialData() {
        viewModelScope.launch {
            try {
                val accounts = accountUseCase.getAccountsFromRoom().first()
                refreshCurrenciesAndFilter(
                    currency = selectedCurrency,
                    accounts = accounts
                )
            } catch (e: Exception) {
                _currencyEditState.value = CurrencyEditState.Error
                _uiEvent.emit(UiEditEvent.ShowSnackbar("Error: ${e.message}"))
            }
        }
    }

    private fun refreshCurrenciesAndFilter(currency: CurrencyUiModel, accounts: List<Account>) {

        viewModelScope.launch {
            runCatching {
                getRatesUseCase(currency.currencyCode, INITIAL_RATE_VALUE)

            }.onSuccess { currencyList ->
                val currencyUiList = currencyList.map { currencyItem ->
                    uiMapper.currencyEntityToCurrencyUi(
                        currencyItem, isSelected = currencyItem.code.name == currency.currencyCode
                    )
                }.map {currencyUiModel ->
                    if (currencyUiModel.currencyCode == currency.currencyCode) {
                        currencyUiModel.copy(amount = currency.amount, rateValue = currency.rateValue)
                    } else {
                        currencyUiModel.copy(rateValue = currencyUiModel.rateValue * currency.amount)
                    }

                }

                val filtered = filterCurrenciesByBalance(
                    currencyUiList,
                    currency.currencyCode,
                    accounts,
                )


                _currencyEditState.value = CurrencyEditState.Success(
                    currencies = filtered,
                    accounts = accounts,
                    enteredAmount = currency.amount,
                    selectedCurrencyCode = currency.currencyCode
                )

            }.onFailure { error ->
                _currencyEditState.value = CurrencyEditState.Error
                _uiEvent.emit(
                    UiEditEvent.ShowSnackbar(
                        error.message ?: "Error while downloading rates"
                    )
                )
            }
        }
    }

    fun updateAmount(currency: CurrencyUiModel) {

        viewModelScope.launch {
            val currentState = _currencyEditState.value
            if (currentState is CurrencyEditState.Success) {

                val accounts = currentState.accounts
                refreshCurrenciesAndFilter(currency, accounts)

            }
        }
    }

    fun getUpdatedCurrenciesForExchange(
        selectedTo: CurrencyUiModel,
    ): Pair<CurrencyUiModel, CurrencyUiModel>? {

        val currentState = _currencyEditState.value
        if (currentState is CurrencyEditState.Success) {

            val source =
                currentState.currencies.find { it.currencyCode == currentState.selectedCurrencyCode }
            val amount = currentState.enteredAmount
            val target =
                currentState.currencies.find { it.currencyCode == selectedTo.currencyCode }

            if (source != null && target != null) {

                val updatedSell = source.copy(amount = amount)
                val updatedBuy = target.copy(amount = target.rateValue)

                return updatedSell to updatedBuy
            }
        }
        return null
    }


    private fun filterCurrenciesByBalance(
        currencies: List<CurrencyUiModel>,
        selectedCurrencyCode: String,
        accounts: List<Account>,
    ): List<CurrencyUiModel> {

        return currencies.filter { currency ->
            if (currency.currencyCode == selectedCurrencyCode) {
                true
            } else {
                val account =
                    accounts.find { it.code.name == currency.currencyCode } ?: return@filter false

                account.balance >= currency.rateValue
            }
        }
    }

    companion object {
        private const val INITIAL_RATE_VALUE = 1.0
        private val CURRENCY_KEY = "Currency"
        private const val TAG = "CurrencyEditViewModel"

    }
}
