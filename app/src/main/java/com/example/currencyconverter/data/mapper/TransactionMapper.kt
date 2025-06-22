package com.example.currencyconverter.data.mapper

import com.example.currencyconverter.data.dataSource.room.transaction.dbo.TransactionDbo
import com.example.currencyconverter.domain.entity.Transaction
import javax.inject.Inject

class TransactionMapper @Inject constructor() {

    fun toTransactionEntity(dbo: TransactionDbo): Transaction {
        return Transaction(
            id = dbo.id,
            to = dbo.to,
            from = dbo.from,
            fromAmount = dbo.fromAmount,
            toAmount = dbo.toAmount,
            dateTime = dbo.dateTime
        )
    }


    fun toTransactionDbo(entity: Transaction): TransactionDbo {
        return TransactionDbo(
            id = entity.id,
            to = entity.to,
            from = entity.from,
            fromAmount = entity.fromAmount,
            toAmount = entity.toAmount,
            dateTime = entity.dateTime
        )
    }
}