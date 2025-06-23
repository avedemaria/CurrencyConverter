package com.example.currencyconverter.domain.repository

import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.CurrencyItem
import com.example.currencyconverter.domain.entity.Transaction
import kotlinx.coroutines.flow.Flow

interface Repository {

    suspend fun getRatesWithInfo(
        baseCurrencyCode: String,
        amount: Double,
    ): List<CurrencyItem>


    suspend fun ensureCurrencyInfoLoaded()


    suspend fun getAccountsFromRoom(): Flow<List<Account>>

    suspend fun saveTransaction(
        transaction: Transaction,
        updatedFrom: Account,
        updatedTo: Account,
    )

    suspend fun getTransactions (): List<Transaction>

}