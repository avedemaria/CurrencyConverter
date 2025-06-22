package com.example.currencyconverter.domain.usecases

import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AccountUseCase @Inject constructor(private val repository: Repository) {


    suspend fun getAccountsFromRoom(): Flow<List<Account>> {
        return repository.getAccountsFromRoom()
    }
}