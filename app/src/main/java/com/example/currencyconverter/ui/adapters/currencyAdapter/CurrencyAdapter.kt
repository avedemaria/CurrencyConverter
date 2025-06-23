package com.example.currencyconverter.ui.adapters.currencyAdapter

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
        return if (screenMode == CurrencyScreenMode.INPUT_MODE && item.isSelected) {
            VIEW_TYPE_INPUT
        } else {
            VIEW_TYPE_LIST
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

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
                CurrencyInputViewHolder(
                    CurrencyItemInputModeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            else -> throw IllegalArgumentException("Invalid View Type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val item = getItem(position)
        when (holder) {
            is CurrencyItemViewHolder -> {
                holder.bind(item)
            }
            is CurrencyInputViewHolder -> {
                holder.bind(item, listener::onAmountChanged)
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
                            is CurrencyChangePayLoad.RateValue -> holder
                                .bindRateAmount(payload.newAmount, payload.symbol)
                            is CurrencyChangePayLoad.Balance -> holder
                                .bindBalance(payload.newBalance, payload.symbol)
                            else -> holder.bind(item)
                        }
                    }
                }

                is CurrencyInputViewHolder -> {
                    for (payload in payloads) {
                        when (payload) {
                            is CurrencyChangePayLoad.EnteredAmount -> {}
                            else -> holder.bind(item, listener::onAmountChanged)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_LIST = 0
        private const val VIEW_TYPE_INPUT = 1
    }
}