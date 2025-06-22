package com.example.currencyconverter.ui.screens.exchangeScreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.currencyconverter.databinding.FragmentExchangeBinding
import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.Currency
import com.example.currencyconverter.ui.CurrencyUiModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ExchangeFragment : Fragment() {


    private val args: ExchangeFragmentArgs by navArgs()

    private var _binding: FragmentExchangeBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException(
            "FragmentExchangeBinding is null"
        )

    private val viewModel: ExchangeViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentExchangeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        initUi()

        viewModel.loadExchangeRate(
            baseCurrencyCode = args.currencyFrom.currencyCode,
            targetCurrencyCode = args.currencyTo.currencyCode,
            amount = 1.0
        )

        binding.btnExchange.setOnClickListener {
            val fromAccount = getFromAccount()
            val toAccount = getToAccount()
            val fromAmount = args.currencyFrom.amount
            val toAmount = args.currencyTo.amount
            Log.d("ExchangeDebug", "fromAmount=$fromAmount, toAmount=$toAmount")

            viewModel.saveTransaction(fromAccount, toAccount, fromAmount, toAmount)
            launchCurrencyListFragment()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })

    }

    private fun observeViewModel() {
        handleState()
        handleEvent()
    }


    private fun handleState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.exchangeState.collect { state ->
                    when (state) {
                        is ExchangeState.Error -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.content.visibility = View.GONE
                        }

                        is ExchangeState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.content.visibility = View.GONE
                        }

                        is ExchangeState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.content.visibility = View.VISIBLE

                            val rate = state.exchangeRate
                            updateExchangeRate(rate)
                        }
                    }
                }
            }
        }
    }


    private fun handleEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    when (event) {
                        is UiExchangeEvent.ShowSnackbar -> {
                            showSnackbar(event.message)
                        }

                        UiExchangeEvent.NavigateToCurrencyList -> {
                            launchCurrencyListFragment()
                        }

                    }
                }
            }
        }
    }


    private fun initUi() {
        val buyCurrency = args.currencyTo
        val sellCurrency = args.currencyFrom

        Log.d("ExchangeFragment", "currencyFrom = $buyCurrency")
        Log.d("ExchangeFragment", "currencyTo = $sellCurrency")

        setUpViews(buyCurrency, sellCurrency)
    }


    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun setUpViews(
        buyCurrency: CurrencyUiModel,
        sellCurrency: CurrencyUiModel,
    ) {

        binding.tvTitle.text = "${buyCurrency.currencyName} to ${sellCurrency.currencyName}"

        binding.ivBuyFlag.setImageResource(buyCurrency.drawableId)
        binding.tvBuyCode.text = buyCurrency.currencyCode
        binding.tvBuyName.text = buyCurrency.currencyName
        Log.d("ExchangeDebug", "tv валюта с плюсом text = '${buyCurrency.amount}'" )
        binding.tvBuyAmount.text =
            String.format("+%s %.2f", buyCurrency.symbol, buyCurrency.amount)

        binding.ivSellFlag.setImageResource(sellCurrency.drawableId)
        binding.tvSellCode.text = sellCurrency.currencyCode
        binding.tvSellName.text = sellCurrency.currencyName

        binding.tvSellAmount.text = "-${sellCurrency.symbol}${sellCurrency.amount}"


        binding.tvSellBalance.text = String.format("Balance: %.2f", sellCurrency.balance)

        binding.btnExchange.text =
            "Buy ${buyCurrency.currencyName} for ${sellCurrency.currencyName}"

    }

    private fun updateExchangeRate(rate: Double) {
        val buyCurrency = args.currencyTo
        val sellCurrency = args.currencyFrom
        binding.tvExchangeRate.text = String.format(
            "%s 1 = %s %.2f",
            buyCurrency.symbol,
            sellCurrency.symbol,
            rate
        )
    }


    private fun getFromAccount(): Account {
        val sellCurrency = args.currencyFrom
        return Account(
            code = Currency.valueOf(sellCurrency.currencyCode),
            balance = sellCurrency.balance
        )
    }

    private fun getToAccount(): Account {
        val buyCurrency = args.currencyTo
        return Account(
            code = Currency.valueOf(buyCurrency.currencyCode),
            balance = buyCurrency.balance
        )
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun launchCurrencyListFragment() {
        findNavController().navigate(ExchangeFragmentDirections.actionExchangeFragmentToCurrencyListFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "CurrencyEditFragment"
    }
}