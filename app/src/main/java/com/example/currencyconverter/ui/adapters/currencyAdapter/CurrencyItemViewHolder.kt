package com.example.currencyconverter.ui.adapters.currencyAdapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.currencyconverter.R
import com.example.currencyconverter.databinding.CurrencyItemBinding
import com.example.currencyconverter.ui.CurrencyUiModel

class CurrencyItemViewHolder(
    private val binding: CurrencyItemBinding,
    private val listener: OnCurrencyClickedListener,
) : RecyclerView.ViewHolder(binding.root) {


    init {
        binding.root.setOnClickListener {
            val currency = it.tag as? CurrencyUiModel
            currency?.let { listener.onCurrencyClicked(it) }
        }

        binding.tvAmount.setOnClickListener {
            val currency = it.tag as? CurrencyUiModel
            currency?.let { listener.onAmountClicked(it) }
        }
    }

    @SuppressLint("DefaultLocale")
    fun bind(currency: CurrencyUiModel) {
        with(binding) {
            tvCode.text = currency.currencyCode
            tvCurrencyName.text = currency.currencyName
            tvAmount.text = String.format("%s %.5f", currency.symbol, currency.rateValue)
            tvBalance.text = String.format("Balance: %s %.2f", currency.symbol, currency.balance)

            ivFlag.load(currency.drawableId) {
                crossfade(true)
                placeholder(R.drawable.landscape_placeholder)
                error(R.drawable.landscape_placeholder)
            }


            root.tag = currency
            tvAmount.tag = currency
        }
    }


    @SuppressLint("DefaultLocale")
    fun bindRateAmount(newAmount: Double, symbol: String) {
        binding.tvAmount.text = String.format("%s %.5f", symbol, newAmount)
    }

    @SuppressLint("DefaultLocale")
    fun bindBalance(newBalance: Double, symbol: String) {
        binding.tvAmount.text = String.format("Balance: %s %.2f", symbol, newBalance)
    }

    fun bindIsSelected(isSelected: Boolean) {
        binding.root.isSelected = isSelected

    }


}