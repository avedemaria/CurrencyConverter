package com.example.currencyconverter.ui.adapters.transactionAdapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.currencyconverter.databinding.TransactionItemBinding
import com.example.currencyconverter.domain.entity.Transaction

class TransactionAdapter :
    ListAdapter<Transaction, TransactionViewHolder>(TransactionDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        return TransactionViewHolder(
            TransactionItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}