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
            oldItem.rateValue != newItem.rateValue -> {
                CurrencyChangePayLoad.RateAmount(newItem.rateValue, newItem.symbol)
            }

            oldItem.balance != newItem.balance -> {
                CurrencyChangePayLoad.Balance(newItem.balance ?: 0.0, newItem.symbol)
            }

            oldItem.isSelected != newItem.isSelected -> {
                CurrencyChangePayLoad.IsSelected(newItem.isSelected)
            }

            else -> super.getChangePayload(oldItem, newItem)
        }
    }
}