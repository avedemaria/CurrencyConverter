package com.example.currencyconverter.ui.screens.exchangeScreen

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.currencyconverter.utils.hide
import com.example.currencyconverter.utils.show
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@SuppressLint("DefaultLocale", "SetTextI18n")
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
            baseCurrencyCode = args.currencySell.currencyCode,
            targetCurrencyCode = args.currencyBuy.currencyCode
        )

        binding.btnExchange.setOnClickListener {

            val fromAccount = getFromAccount()
            val toAccount = getToAccount()
            val fromAmount = args.currencySell.amount
            val toAmount = args.currencyBuy.amount

            if (fromAmount > fromAccount.balance) {
                showSnackbar("Your balance is too low")
                return@setOnClickListener
            }

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
                            binding.errorContent.show()
                            binding.errorContent.setOnRetryClickListener {
                                viewModel.loadExchangeRate(
                                    baseCurrencyCode = args.currencySell.currencyCode,
                                    targetCurrencyCode = args.currencyBuy.currencyCode,
                                )
                            }
                            binding.progressBar.hide()
                            binding.content.hide()
                        }

                        is ExchangeState.Loading -> {
                            binding.progressBar.show()
                            binding.errorContent.hide()
                            binding.content.hide()
                        }

                        is ExchangeState.Success -> {
                            binding.content.show()
                            binding.progressBar.hide()
                            binding.errorContent.hide()

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
        val buyCurrency = args.currencyBuy
        val sellCurrency = args.currencySell

        setUpViews(buyCurrency, sellCurrency)
    }


    private fun setUpViews(
        buyCurrency: CurrencyUiModel,
        sellCurrency: CurrencyUiModel,
    ) {
        with(binding) {
            tvTitle.text = "${buyCurrency.currencyName} to ${sellCurrency.currencyName}"

            ivBuyFlag.setImageResource(buyCurrency.drawableId)
            tvBuyCode.text = buyCurrency.currencyCode
            tvBuyName.text = buyCurrency.currencyName
            tvBuyAmount.text =
                String.format("+%s %.2f", buyCurrency.symbol, buyCurrency.rateValue)

            ivSellFlag.setImageResource(sellCurrency.drawableId)
            tvSellCode.text = sellCurrency.currencyCode
            tvSellName.text = sellCurrency.currencyName
            tvSellAmount.text = "-${sellCurrency.symbol}${sellCurrency.amount}"
            tvSellBalance.text = String.format("Balance: %.2f", sellCurrency.balance)

            btnExchange.text = "Buy ${buyCurrency.currencyName} for ${sellCurrency.currencyName}"
        }

    }

    private fun updateExchangeRate(rate: Double) {
        val buyCurrency = args.currencyBuy
        val sellCurrency = args.currencySell

        binding.tvExchangeRate.text = String.format(
            "%s 1 = %s %.2f",
            buyCurrency.symbol,
            sellCurrency.symbol,
            rate
        )
    }

    private fun getFromAccount(): Account {
        val sellCurrency = args.currencySell
        return Account(
            code = Currency.valueOf(sellCurrency.currencyCode),
            balance = sellCurrency.balance
        )
    }

    private fun getToAccount(): Account {
        val buyCurrency = args.currencyBuy
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