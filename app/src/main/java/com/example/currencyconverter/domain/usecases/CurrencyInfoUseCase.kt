package com.example.currencyconverter.domain.usecases

import com.example.currencyconverter.domain.repository.Repository
import javax.inject.Inject

class CurrencyInfoUseCase @Inject constructor(private val repository: Repository)  {

    suspend operator fun invoke() {
        repository.ensureCurrencyInfoLoaded()
    }
}