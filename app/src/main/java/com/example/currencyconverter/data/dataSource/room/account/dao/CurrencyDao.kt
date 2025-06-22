package com.example.currencyconverter.data.dataSource.room.account.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.currencyconverter.data.dataSource.room.account.dbo.CurrencyDbo

@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(currencies: List<CurrencyDbo>)

    @Query("SELECT * FROM currency_info")
    suspend fun getAll(): List<CurrencyDbo>


}