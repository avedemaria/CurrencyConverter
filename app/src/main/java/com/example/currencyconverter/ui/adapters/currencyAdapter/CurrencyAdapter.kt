package com.example.currencyconverter.ui.adapters.currencyAdapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverter.databinding.CurrencyItemBinding
import com.example.currencyconverter.databinding.CurrencyItemInputModeBinding
import com.example.currencyconverter.ui.CurrencyUiModel
import com.example.currencyconverter.ui.screens.CurrencyScreenMode

class CurrencyAdapter(
    private val listener: OnCurrencyClickedListener,
    private var screenMode: CurrencyScreenMode,
) :
    ListAdapter<CurrencyUiModel, RecyclerView.ViewHolder>(CurrencyItemDiffCallback()) {


    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        Log.d("CurrencyAdapter", "position=$position code=${item.currencyCode} isSelected=${item.isSelected} screenMode=$screenMode")
        return if (screenMode == CurrencyScreenMode.INPUT_MODE && item.isSelected) {
            VIEW_TYPE_INPUT
        } else {
            VIEW_TYPE_LIST
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.d("CurrencyAdapter", "onCreateViewHolder viewType=$viewType")
        return when (viewType) {
            VIEW_TYPE_LIST -> {
                CurrencyItemViewHolder(
                    CurrencyItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), listener
                )
            }

            VIEW_TYPE_INPUT -> {
                Log.d("CurrencyAdapter", "Creating CurrencyInputViewHolder")
                CurrencyInputViewHolder(
                    CurrencyItemInputModeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ), listener
                )
            }

            else -> throw IllegalArgumentException("Invalid View Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = getItem(position)
        when (holder) {
            is CurrencyInputViewHolder -> {
                Log.d("CurrencyAdapter", "Binding CurrencyInputViewHolder at position $position")
                holder.bind(item, item.isSelected)
            }

            is CurrencyItemViewHolder -> {
                Log.d("CurrencyAdapter", "Binding CurrencyItemViewHolder at position $position")
                holder.bind(item)
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            val item = getItem(position)

            when (holder) {
                is CurrencyItemViewHolder -> {
                    for (payload in payloads) {
                        when (payload) {
                            is CurrencyChangePayLoad.RateValue -> holder.bindRateAmount(payload.newAmount, payload.symbol)
                            is CurrencyChangePayLoad.Balance -> holder.bindBalance(payload.newBalance, payload.symbol)
                            else -> holder.bind(item)
                        }
                    }
                }

                is CurrencyInputViewHolder -> {
                    for (payload in payloads) {
                        when (payload) {
                            is CurrencyChangePayLoad.EnteredAmount -> {}
                            else -> holder.bind(item, item.isSelected)
                        }
                    }
                }
            }
        }
    }

//
//    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
//        if (payloads.isEmpty()) {
//            onBindViewHolder(holder, position)
//        } else {
//            when (holder) {
//                is CurrencyItemViewHolder -> {
//                    for (payload in payloads) {
//                        when (payload) {
//                            is CurrencyChangePayLoad.RateValue -> holder.bindRateAmount(payload.newAmount, payload.symbol)
//                            is CurrencyChangePayLoad.EnteredAmount -> {}
//                            is CurrencyChangePayLoad.Balance -> holder.bindBalance(payload.newBalance, payload.symbol)
//                            is CurrencyChangePayLoad.IsSelected -> holder.bindIsSelected(payload.isSelected)
//                            else -> holder.bind(getItem(position))
//                        }
//                    }
//                }
//                is CurrencyInputViewHolder -> {
//
//
//                    holder.bind(getItem(position), getItem(position).isSelected)
//                }
//            }
//        }
//    }


    companion object {
        private const val VIEW_TYPE_LIST = 0
        private const val VIEW_TYPE_INPUT = 1
    }
}