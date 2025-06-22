package com.example.currencyconverter.ui.screens.currenciesEditScreen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.usecases.AccountUseCase
import com.example.currencyconverter.domain.usecases.GetRatesUseCase
import com.example.currencyconverter.ui.CurrencyUiModel
import com.example.currencyconverter.ui.mapper.CurrencyUiMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject


@OptIn(FlowPreview::class)
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

    private val _lastEnteredAmount = MutableStateFlow(1.0)
    val lastEnteredAmount = _lastEnteredAmount.asStateFlow()


    private var selectedCurrency: CurrencyUiModel =
        savedStateHandle.get<CurrencyUiModel>("Currency")
            ?: throw IllegalArgumentException("No currency passed")


    init {
        viewModelScope.launch {
            loadInitialData()
        }
    }


    private suspend fun loadInitialData() {
        Log.d(TAG, "load initial data")
        val accounts = accountUseCase.getAccountsFromRoom().first()
        refreshRatesAndFilter(1.0, accounts)
    }

    private fun refreshRatesAndFilter(amount: Double, accounts: List<Account>) {

        Log.d(TAG, "refresh rates and filter")

        viewModelScope.launch {
            runCatching {
                getRatesUseCase(selectedCurrency.currencyCode, amount)
            }.onSuccess { currencyList ->
                val currencyUiList = currencyList.map {
                    uiMapper.currencyEntityToCurrencyUi(
                        it,
                        isSelected = it.code.name == selectedCurrency.currencyCode
                    )
                }

                val filtered = filterCurrenciesByBalance(
                    currencyUiList,
                    amount,
                    selectedCurrency.currencyCode,
                    accounts
                )


                _currencyEditState.value = CurrencyEditState.Success(
                    currencies = filtered,
                    accounts = accounts,
                    enteredAmount = amount,
                    selectedCurrencyCode = selectedCurrency.currencyCode
                )

            }.onFailure { error ->
                _currencyEditState.value = CurrencyEditState.Error
                _uiEvent.emit(UiEditEvent.ShowSnackbar(error.message ?: "Error while downloading rates"))
            }
        }
    }


    fun updateAmount(newAmount: Double) {
        viewModelScope.launch {
            Log.d(TAG, "update amount")
            val currentState = _currencyEditState.value
            if (currentState is CurrencyEditState.Success) {
                _lastEnteredAmount.emit(newAmount)
                Log.d(TAG, "update amount last entered amount: $lastEnteredAmount")

                val accounts = currentState.accounts
                refreshRatesAndFilter(newAmount, accounts)

            }
        }
    }


    fun getUpdatedCurrenciesForExchange(
        selectedTo: CurrencyUiModel,
    ): Pair<CurrencyUiModel, CurrencyUiModel>? {
        Log.d(TAG, "get updated currencies for exchange")
        val currentState = _currencyEditState.value
        if (currentState is CurrencyEditState.Success) {
            val source =
                currentState.currencies.find { it.currencyCode == currentState.selectedCurrencyCode }
            val amount = currentState.enteredAmount
            val target = selectedTo

            if (source != null) {
                val updatedFrom = source.copy(amount = amount)
                val updatedTo = target.copy(amount = amount * target.rateValue)

                return updatedFrom to updatedTo
            }
        }
        return null
    }

    fun resetAmount() {
        updateAmount(1.0)
    }


//    fun setSelectedCurrency(newCurrency: CurrencyUiModel) {
//        if (newCurrency.currencyCode != selectedCurrency.currencyCode) {
//            selectedCurrency = newCurrency
//            updateAmount(lastEnteredAmount)
//        }
//    }


    private fun filterCurrenciesByBalance(
        currencies: List<CurrencyUiModel>,
        enteredAmount: Double,
        selectedCurrencyCode: String,
        accounts: List<Account>,
    ): List<CurrencyUiModel> {

        val selectedCurrency = currencies.find { it.currencyCode == selectedCurrencyCode }
            ?: return emptyList()

        Log.d(TAG, "filter available currencies")

        return currencies.filter { currency ->
            if (currency.currencyCode == selectedCurrencyCode) {
                true
            } else {
                val account = accounts.find { it.code.name == currency.currencyCode }
                    ?: return@filter false

              val  requiredAmount = enteredAmount * currency.rateValue / selectedCurrency.rateValue

                Log.d(TAG, "selectedCurrency=$selectedCurrencyCode, currency=${currency.currencyCode}, balance=${account.balance}, required=$requiredAmount")

                account.balance >= requiredAmount
            }
        }
    }



   companion object {
        private const val TAG = "CurrencyEditViewModel"
    }
}
