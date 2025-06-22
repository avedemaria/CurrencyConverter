package com.example.currencyconverter.ui.adapters.transactionAdapter

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverter.databinding.TransactionItemBinding
import com.example.currencyconverter.domain.entity.Transaction
import com.example.currencyconverter.utils.DateTimeUtils


class TransactionViewHolder(
    private val binding: TransactionItemBinding,
) : RecyclerView.ViewHolder(binding.root) {


    @SuppressLint("DefaultLocale")
    fun bind(transaction: Transaction) {
        with(binding) {
            tvCurrencyPair.text = String.format("%s → %s", transaction.from, transaction.to)
            tvAmounts.text =
                String.format(
                    "-%s %.2f / +%s %.2f",
                    transaction.from,
                    transaction.fromAmount,
                    transaction.to,
                    transaction.toAmount
                )
            tvDateTime.text = DateTimeUtils.format(transaction.dateTime)
        }

    }
}