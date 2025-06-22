package com.example.currencyconverter.data.mapper

import com.example.currencyconverter.data.dataSource.room.account.dbo.AccountDbo
import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.Currency
import javax.inject.Inject

class AccountMapper @Inject constructor() {


    fun toAccountEntity(dbo: AccountDbo): Account {

        val code = try {
            Currency.valueOf(dbo.code)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Unknown currency: ${dbo.code}")
        }

        return Account(code, dbo.amount)
    }


    fun toAccountDbo(entity: Account): AccountDbo {
        return AccountDbo(entity.code.name, entity.balance)
    }
}