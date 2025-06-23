package com.example.currencyconverter.ui.adapters.currencyAdapter

import com.example.currencyconverter.ui.CurrencyUiModel

interface OnCurrencyClickedListener {

    fun onCurrencyClicked(currency:CurrencyUiModel)

    fun onAmountClicked (currency: CurrencyUiModel)

    fun onAmountChanged(currency: CurrencyUiModel)

}