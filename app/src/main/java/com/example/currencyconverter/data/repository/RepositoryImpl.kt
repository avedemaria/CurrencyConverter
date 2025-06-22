package com.example.currencyconverter.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.example.currencyconverter.data.dataSource.remote.CurrencyApiService
import com.example.currencyconverter.data.dataSource.remote.RatesService
import com.example.currencyconverter.data.dataSource.room.ConverterDatabase
import com.example.currencyconverter.data.dataSource.room.account.dao.AccountDao
import com.example.currencyconverter.data.dataSource.room.account.dao.CurrencyDao
import com.example.currencyconverter.data.dataSource.room.account.dbo.AccountDbo
import com.example.currencyconverter.data.dataSource.room.account.dbo.CurrencyDbo
import com.example.currencyconverter.data.dataSource.room.transaction.dao.TransactionDao
import com.example.currencyconverter.data.dataSource.room.transaction.dbo.TransactionDbo
import com.example.currencyconverter.data.mapper.AccountMapper
import com.example.currencyconverter.data.mapper.CurrencyMapper
import com.example.currencyconverter.data.mapper.TransactionMapper
import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.CurrencyItem
import com.example.currencyconverter.domain.entity.Transaction
import com.example.currencyconverter.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class RepositoryImpl @Inject constructor(
    private val ratesService: RatesService,
    private val currencyApiService: CurrencyApiService,
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val currencyDao: CurrencyDao,
    private val currencyMapper: CurrencyMapper,
    private val accountMapper: AccountMapper,
    private val transactionMapper: TransactionMapper,
    private val database: ConverterDatabase,
) : Repository {


//    override suspend fun getRatesWithInfo(baseCurrencyCode: String, amount: Double): List<Rate> =
//        runCatching {
//            val rateDtos = ratesService.getRates(baseCurrencyCode, amount)
//            val accounts = accountDao.getAll()
//
//            val currencyInfoMap = currencyApiService
//                .getCurrencyInfo()
//                .takeIf { it.isSuccessful }
//                ?.body()
//                ?.data
//                ?: return emptyList()
//
//            rateDtos.map { dto ->
//                val account = accounts.find { it.code == dto.currency }
//                val currencyInfo = currencyInfoMap[dto.currency]
//                mapper.toRateEntity(dto, account, currencyInfo)
//            }
//        }.getOrElse { e ->
//            Log.e(TAG, "Error while fetching data: ${e.message}", e)
//            emptyList()
//        }

    override suspend fun getAvailableCurrencies(
        baseCurrencyCode: String
    ): List<CurrencyItem> = runCatching {

        val rateDtos = ratesService.getRates(baseCurrencyCode, 1.0) // фиксируем amount = 1.0
        val accounts = accountDao.getAll()

        val currencyInfoList = currencyDao.getAll().takeIf { it.isNotEmpty() }
            ?: currencyApiService.getCurrencyInfo()
                .takeIf { it.isSuccessful }
                ?.body()
                ?.data
                ?.map { (code, info) -> CurrencyDbo(code, info.fullName, info.symbol) }
                ?.also { currencyDao.insertAll(it) }
            ?: emptyList()

        val currencyInfoMap = currencyInfoList.associateBy { it.code }

        rateDtos.mapNotNull { dto ->
            val account = accounts.find { it.code == dto.code }
            val currencyInfo = currencyInfoMap[dto.code]
            if (currencyInfo != null) {
                currencyMapper.toCurrencyEntity(dto, account, currencyInfo)
            } else {
                null
            }
        }
    }.getOrElse { e ->
        Log.e(TAG, "Error while fetching data: ${e.message}", e)
        emptyList()
    }



    override suspend fun getRatesWithInfo(
        baseCurrencyCode: String,
        rateValue: Double,
    ): List<CurrencyItem> = runCatching {
        val rateDtos = ratesService.getRates(baseCurrencyCode, rateValue)
        val accounts = accountDao.getAll()

        val currencyInfoList = currencyDao.getAll().takeIf { it.isNotEmpty() }
            ?: currencyApiService.getCurrencyInfo()
                .takeIf { it.isSuccessful }
                ?.body()
                ?.data
                ?.map { (code, info) -> CurrencyDbo(code, info.fullName, info.symbol) }
                ?.also { currencyDao.insertAll(it) }
            ?: emptyList()

        val currencyInfoMap = currencyInfoList.associateBy { it.code }

        rateDtos.mapNotNull { dto ->
            val account = accounts.find { it.code == dto.code }
            val currencyInfo = currencyInfoMap[dto.code]
            if (currencyInfo != null) {
                currencyMapper.toCurrencyEntity(dto, account, currencyInfo)
            } else {
                null
            }
        }
    }.getOrElse { e ->
        Log.e(TAG, "Error while fetching data: ${e.message}", e)
        emptyList()
    }


    override suspend fun getAccountsFromRoom(): Flow<List<Account>> {
        return accountDao.getAllAsFlow()
            .onStart {
                runCatching {
                   insertDefaultAccounts()
                }.onFailure { error ->
                    Log.e(TAG, "error while inserting RUB: ${error.message}", error)
                }
            }
            .map { accountsDbo ->
                accountsDbo.map { dbo ->
                    accountMapper.toAccountEntity(dbo)
                }
            }
            .catch { error ->
                Log.e(TAG, "Error in Flow: ${error.message}", error)
                emit(emptyList())
            }
    }

    override suspend fun ensureCurrencyInfoLoaded() {
        val cached = currencyDao.getAll()
        if (cached.isEmpty()) {
            val response = currencyApiService.getCurrencyInfo()
            if (response.isSuccessful) {
                val data = response.body()?.data ?: return
                val list = data.map { (code, info) ->
                    CurrencyDbo(code = code, name = info.fullName, symbol = info.symbol)
                }
                currencyDao.insertAll(list)
            }
        }
    }


    override suspend fun saveTransaction(
        transaction: Transaction,
        updatedFrom: Account,
        updatedTo: Account,
    ) {
        runCatching {
            database.withTransaction {
                val transactionDbo = transactionMapper.toTransactionDbo(transaction)
                transactionDao.insertAll(transactionDbo)

                val fromDbo = accountMapper.toAccountDbo(updatedFrom)
                val toDbo = accountMapper.toAccountDbo(updatedTo)

                accountDao.insertAll(fromDbo)
                accountDao.insertAll(toDbo)
            }
        }.onFailure { e ->
            Log.e(TAG, "error while inserting transaction and updating accounts: ${e.message}", e)
        }
    }

    override suspend fun getTransactions(): List<Transaction> {
        return kotlin.runCatching {
            transactionDao.getAll().map { dbo ->
                transactionMapper.toTransactionEntity(dbo)
            }
        }
            .getOrElse { e ->
                Log.e(TAG, "Error while fetching transactions: ${e.message}", e)
                emptyList()
            }
    }

    private suspend fun insertDefaultAccounts () {
            val defaultAccounts = listOf(
                AccountDbo(code = "RUB", amount = 75000.0),
                AccountDbo(code = "USD", amount = 1000.0),
                AccountDbo(code = "EUR", amount = 450.0)
            )
            val existingCodes = accountDao.getAll().map { it.code }

            val accounts = defaultAccounts.filter { it.code !in existingCodes }

            if (accounts.isNotEmpty()) {
                accountDao.insertAll(*accounts.toTypedArray())
            }

    }

    companion object {
        private const val TAG = "RepositoryImpl"
    }


}