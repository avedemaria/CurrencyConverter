package com.example.currencyconverter.data.mapper

import com.example.currencyconverter.data.dataSource.remote.dto.RateDto
import com.example.currencyconverter.data.dataSource.room.account.dbo.AccountDbo
import com.example.currencyconverter.data.dataSource.room.account.dbo.CurrencyDbo
import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.Currency
import com.example.currencyconverter.domain.entity.CurrencyItem
import javax.inject.Inject


class CurrencyMapper @Inject constructor(
    private val accountMapper: AccountMapper,
) {

    fun toCurrencyEntity(
        rateDto: RateDto,
        accountDbo: AccountDbo?,
        currencyDbo: CurrencyDbo,
    ): CurrencyItem {
        val currencyEnum = try {
            Currency.valueOf(rateDto.code)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Unknown currency: ${rateDto.code}")
        }

        val account = accountDbo?.let { accountMapper.toAccountEntity(it) }
            ?: Account(currencyEnum, 0.0)


        return CurrencyItem(
            symbol = currencyDbo.symbol ?: "*",
            account = account,
            code = currencyEnum,
            name = currencyDbo.name,
            rateValue = rateDto.value
        )
    }


}

