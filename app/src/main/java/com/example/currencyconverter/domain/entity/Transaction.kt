package com.example.currencyconverter.domain.entity

import java.time.LocalDateTime

data class Transaction(
    val id: Int,
    val to: String,
    val from:String,
    val fromAmount: Double,
    val toAmount: Double,
    val dateTime: LocalDateTime,
)