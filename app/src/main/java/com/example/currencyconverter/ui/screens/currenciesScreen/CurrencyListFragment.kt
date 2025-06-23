package com.example.currencyconverter.ui.screens.currenciesScreen

import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverter.R
import com.example.currencyconverter.databinding.FragmentCurrencyListBinding
import com.example.currencyconverter.ui.CurrencyUiModel
import com.example.currencyconverter.ui.adapters.currencyAdapter.CurrencyAdapter
import com.example.currencyconverter.ui.adapters.currencyAdapter.OnCurrencyClickedListener
import com.example.currencyconverter.ui.screens.CurrencyScreenMode
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CurrencyListFragment : Fragment() {

    private var _binding: FragmentCurrencyListBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException(
            "FragmentCurrencyListBinding is null"
        )

    private val viewModel: CurrencyListViewModel by viewModels()

    private lateinit var currenciesAdapter: CurrencyAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCurrencyListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setUpAdapter()
        setUpListeners()
    }

    private fun setUpListeners() {

        binding.fabTransactions.setOnClickListener {
            launchTransactionsFragment()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })


        binding.errorView.setOnRetryClickListener {
            viewModel.startAutoRefresh()
        }
    }


    private fun setUpAdapter() {

        currenciesAdapter = CurrencyAdapter(
            listener = object : OnCurrencyClickedListener {
                override fun onCurrencyClicked(currency: CurrencyUiModel) {
                    Log.d(TAG, "on currency clicked")
                    viewModel.selectCurrency(currency.currencyCode)
                }

                override fun onAmountClicked(currency: CurrencyUiModel) {
                    Log.d(TAG, "selected rate ${currency.rateValue}")
                    if (currency.balance<=0) {
                        showSnackbar(getString(R.string.balance_is_too_low))
                        return
                    } else {
                        launchEditMode(currency)
                    }
                }

                override fun onAmountChanged(currency: CurrencyUiModel, newAmount: Double) {

                }
            },
            screenMode = CurrencyScreenMode.LIST_MODE
        )

        binding.rvCurrencies.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), RecyclerView.VERTICAL,
                false
            )
            adapter = currenciesAdapter

            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
        }


    }


    private fun observeViewModel() {
        handleState()
        handleEvent()
    }


    private fun handleState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.currencyListState.collect { state ->
                    Log.d(TAG, "cuurent state $state")
                    when (state) {

                        is CurrencyListState.Error -> {
                            binding.errorView.showError()
                            binding.rvCurrencies.visibility = View.GONE
                            binding.fabTransactions.visibility = View.GONE
                            binding.progressBar.visibility = View.GONE
                        }

                        is CurrencyListState.Loading -> {
                            binding.errorView.hide()
                            binding.rvCurrencies.visibility = View.GONE
                            binding.fabTransactions.visibility = View.GONE
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is CurrencyListState.Success -> {
                            binding.errorView.hide()
                            binding.rvCurrencies.visibility = View.VISIBLE
                            binding.fabTransactions.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE

                            currenciesAdapter.submitList(state.currencies)
                        }

                        is CurrencyListState.MoveToTop -> {
                            currenciesAdapter.submitList(state.currencies) {
                                binding.rvCurrencies.scrollToPosition(0)
                            }
                            viewModel.stopAutoRefresh()
                            viewModel.startAutoRefresh()
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
                        is UiEvent.ShowSnackbar -> showSnackbar(event.message)
                    }
                }
            }
        }
    }


    private fun launchEditMode(currency: CurrencyUiModel) {
        findNavController().navigate(
            CurrencyListFragmentDirections.actionCurrencyListFragmentToCurrencyEditFragment(
                currency
            )
        )
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }


    private fun launchTransactionsFragment() {
        findNavController().navigate(
            CurrencyListFragmentDirections.actionCurrencyListFragmentToTransactionsFragment()
        )
    }

    override fun onStart() {
        super.onStart()
        viewModel.startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopAutoRefresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "CurrencyListFragment"
    }
}