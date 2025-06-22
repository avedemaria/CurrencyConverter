package com.example.currencyconverter.domain.usecases


import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.Transaction
import com.example.currencyconverter.domain.repository.Repository
import javax.inject.Inject

class TransactionUseCase @Inject constructor(private val repository: Repository) {

    suspend fun saveTransaction(
        transaction: Transaction,
        updatedFrom: Account,
        updatedTo: Account,
    ) {
        repository.saveTransaction(transaction, updatedFrom, updatedTo)
    }


    suspend fun getTransactions(): List<Transaction> {
        return repository.getTransactions()
    }
}