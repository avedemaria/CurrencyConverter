package com.example.currencyconverter.data.dataSource.room

import androidx.room.Dao
import androidx.room.Query

@Dao
interface DebugDao {
    @Query("DELETE FROM accounts")
    suspend fun clearAccounts()

    @Query("DELETE FROM currency_info")
    suspend fun clearCurrencies()

    @Query("DELETE FROM transactions")
    suspend fun clearTransactions()
}