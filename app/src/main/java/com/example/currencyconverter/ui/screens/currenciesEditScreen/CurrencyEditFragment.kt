package com.example.currencyconverter.ui.screens.currenciesEditScreen

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
import com.example.currencyconverter.databinding.FragmentCurrencyEditBinding
import com.example.currencyconverter.ui.CurrencyUiModel
import com.example.currencyconverter.ui.adapters.currencyAdapter.CurrencyAdapter
import com.example.currencyconverter.ui.adapters.currencyAdapter.OnCurrencyClickedListener
import com.example.currencyconverter.ui.screens.currenciesScreen.CurrencyScreenMode
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CurrencyEditFragment : Fragment() {


    private var _binding: FragmentCurrencyEditBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException(
            "FragmentCurrencyEditBinding is null"
        )

    private val viewModel: CurrencyEditViewModel by viewModels()

    private lateinit var currenciesAdapter: CurrencyAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCurrencyEditBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setUpAdapter()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }


    private fun setUpAdapter() {

        currenciesAdapter = CurrencyAdapter(
            object : OnCurrencyClickedListener {
                override fun onCurrencyClicked(currency: CurrencyUiModel) {
                    viewModel.getUpdatedCurrenciesForExchange(currency)?.let { (from, to) ->
                        launchExchangeFragment(from, to)
                    }
                }

                override fun onAmountClicked(currency: CurrencyUiModel) {

                }

                override fun onAmountChanged(currency: CurrencyUiModel, newAmount: Double) {
                    Log.d(TAG, "Amount changed: $newAmount for currency ${currency.currencyCode}")
                    viewModel.updateAmount(newAmount)
                }
            },
            screenMode = CurrencyScreenMode.INPUT_MODE
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
                    when (state) {
                        is CurrencyEditState.Error -> {
                            binding.rvCurrencies.visibility = View.GONE
                            binding.progressBar.visibility = View.GONE
                        }
                        is CurrencyEditState.Loading -> {
                            binding.rvCurrencies.visibility = View.GONE
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is CurrencyEditState.Success -> {
                            binding.rvCurrencies.visibility = View.VISIBLE
                            binding.progressBar.visibility = View.GONE

                            currenciesAdapter.submitList(state.currencies)
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
                        is UiEditEvent.ShowSnackbar -> showSnackbar(event.message)
                    }
                }
            }
        }
    }


    private fun launchExchangeFragment(
        currencyFrom: CurrencyUiModel,
        currencyTo: CurrencyUiModel,
    ) {
        findNavController().navigate(
            CurrencyEditFragmentDirections.actionCurrencyEditFragmentToExchangeFragment(
                currencyTo,
                currencyFrom
            )
        )
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "CurrencyEditFragment"
    }
}