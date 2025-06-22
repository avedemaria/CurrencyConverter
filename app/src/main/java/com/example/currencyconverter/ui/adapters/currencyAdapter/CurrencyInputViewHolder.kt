package com.example.currencyconverter.ui.adapters.currencyAdapter

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
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
import com.example.currencyconverter.utils.InputUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class CurrencyInputViewHolder(
    private val binding: CurrencyItemInputModeBinding,
    private val listener: OnCurrencyClickedListener
) : RecyclerView.ViewHolder(binding.root) {

    private var textWatcherJob: Job? = null
    private var isUpdating = false


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


            etAmount.textChanges()

            textWatcherJob?.cancel()

            if (isSelected) {
                etAmount.visibility = View.VISIBLE
                tvAmount.visibility = View.GONE
                btnClearAmount.visibility = View.VISIBLE


                if (!etAmount.text.toString().endsWith(".00")) {
                    etAmount.setText("${etAmount.text}.00")
                }
                fixCursorPosition()

                textWatcherJob?.cancel()
                textWatcherJob = CoroutineScope(Dispatchers.Main).launch {
                    etAmount.textChanges()
                        .collect { text ->
                            if (isUpdating) return@collect

                            var raw = text
                            if (!raw.endsWith(".00")) {
                                isUpdating = true
                                raw = raw.replace(".00", "")
                                etAmount.setText("$raw.00")
                                fixCursorPosition()
                                isUpdating = false
                            } else {
                                fixCursorPosition()
                            }


                            val numericPart = raw.removeSuffix(".00")
                            val amount = numericPart.toDoubleOrNull() ?: 0.0
                            listener.onAmountChanged(currency, amount)
                        }
                }

                btnClearAmount.setOnClickListener {
                    isUpdating = true
                    etAmount.setText("0.00")
                    fixCursorPosition()
                    listener.onAmountChanged(currency, 0.0)
                    isUpdating = false
                }


                etAmount.setOnClickListener { fixCursorPosition() }
                etAmount.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) fixCursorPosition()
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



    private fun fixCursorPosition() {
        val position = binding.etAmount.text.length - 3
        if (position >= 0) {
            binding.etAmount.setSelection(position)
        }
    }
}






fun EditText.textChanges(): Flow<String> = callbackFlow {
    val watcher = this@textChanges.doOnTextChanged { text, _, _, _ ->
        trySend(text.toString())
    }
    awaitClose { this@textChanges.removeTextChangedListener(watcher) }
}


