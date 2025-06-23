package com.example.currencyconverter.ui.adapters.currencyAdapter

import android.annotation.SuppressLint
import android.text.method.ScrollingMovementMethod
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.currencyconverter.R
import com.example.currencyconverter.databinding.CurrencyItemBinding
import com.example.currencyconverter.ui.CurrencyUiModel

@SuppressLint("DefaultLocale")
class CurrencyItemViewHolder(
    private val binding: CurrencyItemBinding,
    private val listener: OnCurrencyClickedListener,
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.root.setOnClickListener { _it ->
            val currency = _it.tag as? CurrencyUiModel
            currency?.let { listener.onCurrencyClicked(it) }
        }

        binding.tvRateValue.setOnClickListener {
            val currency = it.tag as? CurrencyUiModel
            currency?.let { listener.onAmountClicked(it) }
        }
    }


    fun bind(currency: CurrencyUiModel) {
        with(binding) {
            tvCode.text = currency.currencyCode
            tvCurrencyName.text = currency.currencyName
            tvRateValue.text = String.format("%s %.5f", currency.symbol, currency.rateValue)
            tvBalance.text = String.format("Balance: %s %.2f", currency.symbol, currency.balance)

            ivFlag.load(currency.drawableId) {
                crossfade(true)
                placeholder(R.drawable.landscape_placeholder)
                error(R.drawable.landscape_placeholder)
            }


            root.tag = currency
            tvRateValue.tag = currency
        }
    }

    fun bindRateAmount(newRateValue: Double, symbol: String) {
        binding.tvRateValue.text = String.format("%s %.5f", symbol, newRateValue)
    }

    fun bindBalance(newBalance: Double, symbol: String) {
        binding.tvBalance.text = String.format("Balance: %s %.2f", symbol, newBalance)
    }

}