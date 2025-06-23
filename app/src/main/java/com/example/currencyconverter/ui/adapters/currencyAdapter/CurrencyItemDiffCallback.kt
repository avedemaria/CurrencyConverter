package com.example.currencyconverter.ui.adapters.currencyAdapter


import androidx.recyclerview.widget.DiffUtil
import com.example.currencyconverter.ui.CurrencyUiModel

class CurrencyItemDiffCallback : DiffUtil.ItemCallback<CurrencyUiModel>() {

    override fun areItemsTheSame(oldItem: CurrencyUiModel, newItem: CurrencyUiModel): Boolean =
        oldItem.currencyCode == newItem.currencyCode

    override fun areContentsTheSame(oldItem: CurrencyUiModel, newItem: CurrencyUiModel): Boolean =
        oldItem == newItem


    override fun getChangePayload(oldItem: CurrencyUiModel, newItem: CurrencyUiModel): Any? {

        return when {
            oldItem.amount != newItem.amount -> {
                CurrencyChangePayLoad.EnteredAmount(newItem.amount)
            }

            oldItem.rateValue != newItem.rateValue -> {
                CurrencyChangePayLoad.RateValue(newItem.rateValue, newItem.symbol)
            }
            oldItem.balance != newItem.balance -> {
                CurrencyChangePayLoad.Balance(newItem.balance, newItem.symbol)
            }

            else -> super.getChangePayload(oldItem, newItem)
        }
    }
}