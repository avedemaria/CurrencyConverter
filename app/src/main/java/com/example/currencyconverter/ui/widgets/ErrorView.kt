package com.example.currencyconverter.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.currencyconverter.databinding.ErrorViewBinding
import com.example.currencyconverter.utils.hide

class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding = ErrorViewBinding.inflate(LayoutInflater.from(context), this)

    init {
       hide()
    }
    fun setOnRetryClickListener(listener: () -> Unit) {
        binding.btnRetry.setOnClickListener { listener() }
    }
}