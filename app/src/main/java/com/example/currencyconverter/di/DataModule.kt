package com.example.currencyconverter.di

import android.content.Context
import androidx.room.Room
import com.example.currencyconverter.data.dataSource.remote.CurrencyApiFactory
import com.example.currencyconverter.data.dataSource.remote.CurrencyApiService
import com.example.currencyconverter.data.dataSource.room.ConverterDatabase
import com.example.currencyconverter.data.dataSource.room.DebugDao
import com.example.currencyconverter.data.dataSource.room.account.dao.AccountDao
import com.example.currencyconverter.data.dataSource.room.account.dao.CurrencyDao
import com.example.currencyconverter.data.dataSource.room.transaction.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DataModule {


    @Singleton
    @Provides
    fun provideCurrencyApiService(): CurrencyApiService {
        return CurrencyApiFactory.provideApiService()
    }


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ConverterDatabase {
        return Room.databaseBuilder(
            context,
            ConverterDatabase::class.java,
            "Converter_DB"
        ).fallbackToDestructiveMigration().build()
    }


    @Singleton
    @Provides
    fun provideCurrencyDao(database: ConverterDatabase): CurrencyDao {
        return database.currencyDao()
    }

    @Singleton
    @Provides
    fun provideDebugDao(database: ConverterDatabase): DebugDao {
        return database.debugDao()
    }

    @Singleton
    @Provides
    fun provideAccountDao(database: ConverterDatabase): AccountDao {
        return database.accountDao()
    }

    @Singleton
    @Provides
    fun provideTransactionDao(database: ConverterDatabase): TransactionDao {
        return database.transactionDao()
    }


}