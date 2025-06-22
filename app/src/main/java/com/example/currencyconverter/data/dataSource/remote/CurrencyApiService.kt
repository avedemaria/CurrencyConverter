package com.example.currencyconverter.data.dataSource.remote

import com.example.currencyconverter.data.dataSource.remote.dto.CurrencyResponse
import retrofit2.Response
import retrofit2.http.GET

interface CurrencyApiService {

    @GET("currencies")
    suspend fun getCurrencyInfo (): Response<CurrencyResponse>
}