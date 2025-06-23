package com.example.currencyconverter.ui.adapters.currencyAdapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.currencyconverter.R
import com.example.currencyconverter.databinding.CurrencyItemInputModeBinding
import com.example.currencyconverter.ui.CurrencyUiModel
import com.example.currencyconverter.ui.screens.currenciesScreen.CurrencyListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CurrencyInputViewHolder(
    private val binding: CurrencyItemInputModeBinding,
    private val listener: OnCurrencyClickedListener
) : RecyclerView.ViewHolder(binding.root) {

    private var textWatcherJob: Job? = null


    @SuppressLint("DefaultLocale")
    fun bind(currency: CurrencyUiModel, isSelected: Boolean) {
        Log.d("CurrencyInputViewHolder", "Binding currency: $currency, isSelected=$isSelected")

        with(binding) {
            tvCode.text = currency.currencyCode
            tvCurrencyName.text = currency.currencyName
            tvBalance.text = String.format("Balance: %s %.2f", currency.symbol, currency.balance)

            ivFlag.load(currency.drawableId) {
                crossfade(true)
                placeholder(R.drawable.landscape_placeholder)
                error(R.drawable.landscape_placeholder)
            }

            tvAmount.text = String.format("%.2f", currency.amount)
            Log.d("CurrencyInputViewHolder", "tv amount currency ${currency.amount}")


            textWatcherJob?.cancel()
            if (isSelected) {
                etAmount.visibility = View.VISIBLE
                tvAmount.visibility = View.GONE
                btnClearAmount.visibility = View.VISIBLE


                etAmount.setText(etAmount.text.toString() + ".00")

                val amountFlow = etAmount.textChanges()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { text ->
                        var value = text.toString()
                        if (!value.endsWith(".00")) {
                            value += ".00"
                            etAmount.setText(value)
                            etAmount.setSelection(value.length - 3)
                        }
                        value.removeSuffix(".00").toDoubleOrNull() ?: 0.0
                    }
                    .distinctUntilChanged()


                textWatcherJob = CoroutineScope(Dispatchers.Main).launch {
                    amountFlow.collect { amount ->
                        Log.d("CurrencyInputViewHolder", "Amount: $amount")
                        listener.onAmountChanged(currency, amount)
                        tvAmount.text = String.format("%.2f", amount)
                        Log.d("CurrencyInputViewHolder", "tv amount currency2 $amount")
                    }
                }

                btnClearAmount.setOnClickListener {
                    etAmount.setText("0.00")
                    etAmount.setSelection(etAmount.text.length - 3)
                    listener.onAmountChanged(currency, 0.0)
                }

            } else {
                etAmount.visibility = View.GONE
                tvAmount.visibility = View.VISIBLE
                btnClearAmount.visibility = View.GONE
                textWatcherJob?.cancel()
            }

            root.tag = currency
        }


        }


//    fun bindAmount(newAmount: Double) {
//        binding.tvAmount.text = String.format("%.2f", newAmount)
//        if (binding.etAmount.visibility == View.VISIBLE) {
//            isUpdating = true
//            binding.etAmount.setText(String.format("%.2f", newAmount))
//            fixCursorPosition()
//            isUpdating = false
//        }
//    }
//
//
//    private fun fixCursorPosition() {
//        val position = binding.etAmount.text.length - 3
//        if (position >= 0) {
//            binding.etAmount.setSelection(position)
//        }
//    }
}

fun EditText.textChanges(): Flow<String> {
    return callbackFlow {
        val listener = doOnTextChanged { text, _, _, _ ->
            trySend(text.toString())
        }
        awaitClose { removeTextChangedListener(listener) }
    }.onStart { emit(text.toString()) }
}



