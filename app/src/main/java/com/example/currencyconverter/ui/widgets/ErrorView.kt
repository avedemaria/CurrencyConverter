package com.example.currencyconverter.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.example.currencyconverter.databinding.ErrorViewBinding

class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding = ErrorViewBinding.inflate(LayoutInflater.from(context), this)

    init {
        visibility = View.GONE
    }

    fun showError() {
        visibility = View.VISIBLE
    }

    fun hide() {
        visibility = View.GONE
    }

    fun setOnRetryClickListener(listener: () -> Unit) {
        binding.btnRetry.setOnClickListener { listener() }
    }
}