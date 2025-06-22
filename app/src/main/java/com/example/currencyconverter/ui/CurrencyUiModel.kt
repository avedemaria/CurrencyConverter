package com.example.currencyconverter.ui

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CurrencyUiModel(
    val currencyCode: String,
    val currencyName: String,
    val symbol: String,
    val drawableId: Int,
    val amount: Double,
    val rateValue: Double,
    val balance: Double,
    val isSelected: Boolean
) : Parcelable