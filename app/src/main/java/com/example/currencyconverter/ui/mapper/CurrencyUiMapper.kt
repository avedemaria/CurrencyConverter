package com.example.currencyconverter.ui.mapper

import com.example.currencyconverter.domain.entity.CurrencyItem
import com.example.currencyconverter.ui.CurrencyUiModel
import com.example.currencyconverter.ui.providers.FlagProvider
import javax.inject.Inject

class CurrencyUiMapper @Inject constructor(
    private val flagProvider: FlagProvider,
) {

    fun currencyEntityToCurrencyUi(
        currencyItem: CurrencyItem,
        isSelected: Boolean,
    ): CurrencyUiModel {
       return CurrencyUiModel(
            currencyCode = currencyItem.code.name,
            currencyName = currencyItem.name,
            symbol = currencyItem.symbol,
            drawableId = flagProvider.getFlagResource(currencyItem.code),
            balance = currencyItem.account.balance,
            isSelected = isSelected,
            amount = 0.0,
            rateValue = currencyItem.rateValue
        )

    }




}