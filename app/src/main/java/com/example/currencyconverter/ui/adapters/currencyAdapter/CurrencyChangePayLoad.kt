package com.example.currencyconverter.ui.adapters.currencyAdapter

sealed interface CurrencyChangePayLoad {

    data class RateAmount(val newAmount: Double, val symbol: String) : CurrencyChangePayLoad
    data class Balance(val newBalance: Double, val symbol:String) : CurrencyChangePayLoad
    data class IsSelected(val isSelected: Boolean) : CurrencyChangePayLoad

}