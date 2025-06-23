package com.example.currencyconverter.data.dataSource.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.currencyconverter.data.dataSource.room.account.dao.AccountDao
import com.example.currencyconverter.data.dataSource.room.account.dao.CurrencyDao
import com.example.currencyconverter.data.dataSource.room.account.dbo.AccountDbo
import com.example.currencyconverter.data.dataSource.room.account.dbo.CurrencyDbo
import com.example.currencyconverter.data.dataSource.room.converter.Converters
import com.example.currencyconverter.data.dataSource.room.transaction.dao.TransactionDao
import com.example.currencyconverter.data.dataSource.room.transaction.dbo.TransactionDbo

@Database(
    entities = [AccountDbo::class, TransactionDbo::class, CurrencyDbo::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ConverterDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun debugDao(): DebugDao

}