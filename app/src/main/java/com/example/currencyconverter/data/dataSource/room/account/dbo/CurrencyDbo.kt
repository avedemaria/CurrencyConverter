package com.example.currencyconverter.data.dataSource.room.account.dbo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currency_info")
class CurrencyDbo(
    @PrimaryKey val code: String,
    val name: String,
    val symbol: String? = null
)