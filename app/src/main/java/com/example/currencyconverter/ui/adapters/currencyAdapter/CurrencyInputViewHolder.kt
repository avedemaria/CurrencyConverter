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

@SuppressLint("DefaultLocale", "SetTextI18n", "ClickableViewAccessibility")
class CurrencyInputViewHolder(
    private val binding: CurrencyItemInputModeBinding,
) : RecyclerView.ViewHolder(binding.root) {

    private var textWatcherJob: Job? = null

    fun bind(
        currency: CurrencyUiModel,
        onChanged: (CurrencyUiModel) -> Unit,
    ) {
        with(binding) {
            tvCode.text = currency.currencyCode
            tvCurrencyName.text = currency.currencyName
            tvBalance.text = String.format("Balance: %s %.2f", currency.symbol, currency.balance)

            ivFlag.load(currency.drawableId) {
                crossfade(true)
                placeholder(R.drawable.landscape_placeholder)
                error(R.drawable.landscape_placeholder)
            }

            textWatcherJob?.cancel()
                etAmount.visibility = View.VISIBLE
                btnClearAmount.visibility = View.VISIBLE

                val fixedSuffix = ".00"
                val initialValue = currency.amount.toInt().toString()
                etAmount.setText(initialValue + fixedSuffix)
                etAmount.setSelection(initialValue.length)

                btnClearAmount.setOnClickListener {
                    etAmount.setText("0$fixedSuffix")
                    etAmount.setSelection(1)
                }

                etAmount.setUpEtAmount { value ->
                    onChanged(currency.copy(amount = value))
                }
            root.tag = currency
        }
    }


    private fun EditText.setUpEtAmount(onChanged: (Double) -> Unit) {
        setOnTouchListener { v, _ ->
            v as EditText
            val dotIndex = v.text.indexOf(".")
            v.post {
                if (v.selectionStart > dotIndex) {
                    v.setSelection(dotIndex)
                }
            }
            false
        }

        doOnTextChanged { text, _, _, _ ->
            val valueText = text.toString()

            val dotIndex = valueText.indexOf(".")
            val numberPart = if (dotIndex != -1) valueText.substring(0, dotIndex) else valueText

            val fixedText = "$numberPart.00"

            if (fixedText != valueText) {
                setText(fixedText)
                setSelection(numberPart.length)
            }

            val value = numberPart.toDoubleOrNull() ?: 0.0
            onChanged(value)
        }
    }
}




