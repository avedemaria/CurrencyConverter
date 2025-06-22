package com.example.currencyconverter.ui.providers

import com.example.currencyconverter.R
import com.example.currencyconverter.domain.entity.Currency
import javax.inject.Inject

class FlagProvider @Inject constructor() {

    private val flagMap = mapOf(
        Currency.AUD to R.drawable.aud_static,
        Currency.BGN to R.drawable.bgn_static,
        Currency.BRL to R.drawable.brl_static,
        Currency.CAD to R.drawable.cad_static,
        Currency.CHF to R.drawable.chf_static,
        Currency.CNY to R.drawable.cny_static,
        Currency.CZK to R.drawable.czk_static,
        Currency.DKK to R.drawable.dkk_static,
        Currency.EUR to R.drawable.eur_static,
        Currency.GBP to R.drawable.gbp_static,
        Currency.HKD to R.drawable.hkd_static,
        Currency.HRK to R.drawable.hrk_static,
        Currency.HUF to R.drawable.huf_static,
        Currency.IDR to R.drawable.idr_static,
        Currency.ILS to R.drawable.ils_static,
        Currency.INR to R.drawable.inr_static,
        Currency.ISK to R.drawable.isk_static,
        Currency.JPY to R.drawable.jpy_static,
        Currency.KRW to R.drawable.krw_static,
        Currency.MXN to R.drawable.mxn_static,
        Currency.MYR to R.drawable.myr_static,
        Currency.NOK to R.drawable.nok_static,
        Currency.NZD to R.drawable.nzd_static,
        Currency.PHP to R.drawable.php_static,
        Currency.PLN to R.drawable.pln_static,
        Currency.RON to R.drawable.ron_static,
        Currency.RUB to R.drawable.rub_static,
        Currency.SEK to R.drawable.sek_static,
        Currency.SGD to R.drawable.sgd_static,
        Currency.THB to R.drawable.thb_static,
        Currency.TRY to R.drawable.try_static,
        Currency.USD to R.drawable.usd_static,
        Currency.ZAR to R.drawable.zar_static
    )


    fun getFlagResource(currency: Currency): Int {
        return flagMap[currency] ?: R.drawable.landscape_placeholder
    }

}