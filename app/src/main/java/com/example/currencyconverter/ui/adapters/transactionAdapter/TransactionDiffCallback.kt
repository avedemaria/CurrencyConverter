package com.example.currencyconverter.ui.adapters.transactionAdapter

import androidx.recyclerview.widget.DiffUtil.ItemCallback
import com.example.currencyconverter.domain.entity.Transaction

class TransactionDiffCallback: ItemCallback<Transaction>(){

    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
       return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
        return oldItem == newItem
    }
}