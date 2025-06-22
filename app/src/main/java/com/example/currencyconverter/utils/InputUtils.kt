package com.example.currencyconverter.utils

import android.text.InputFilter
import android.text.Spanned

object InputUtils: InputFilter {

    override fun filter(
        p0: CharSequence?,
        p1: Int,
        p2: Int,
        p3: Spanned?,
        p4: Int,
        p5: Int,
    ): CharSequence? {
        val currentText = p3?.toString() ?: ""
        if (currentText.endsWith(".00") && p5 > currentText.length - 3) {
            return ""
        }
        return null
    }
}