package com.example.currencyconverter.domain.usecases

import android.util.Log
import com.example.currencyconverter.domain.entity.CurrencyItem
import com.example.currencyconverter.domain.repository.Repository
import javax.inject.Inject

class GetRatesUseCase @Inject constructor(private val repository: Repository) {

    suspend operator fun invoke(baseCurrencyCode: String, rateValue: Double): List<CurrencyItem> {
        return repository.getRatesWithInfo(baseCurrencyCode, rateValue)
    }
}