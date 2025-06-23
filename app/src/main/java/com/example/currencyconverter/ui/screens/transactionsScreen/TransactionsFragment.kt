package com.example.currencyconverter.ui.screens.transactionsScreen

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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.currencyconverter.databinding.FragmentTransactionsBinding
import com.example.currencyconverter.ui.adapters.transactionAdapter.TransactionAdapter
import com.example.currencyconverter.utils.hide
import com.example.currencyconverter.utils.show
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TransactionsFragment : Fragment() {


    private var _binding: FragmentTransactionsBinding? = null
    private val binding
        get() = _binding ?: throw RuntimeException(
            "FragmentTransactionsBinding is null"
        )

    private val viewModel: TransactionViewModel by viewModels()

    private lateinit var transactionsAdapter: TransactionAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeViewModel()
        setUpAdapter()
        setUpListeners()
    }


    private fun setUpListeners() {
        binding.fabBack.setOnClickListener {
            findNavController().popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }


    private fun setUpAdapter() {

        transactionsAdapter = TransactionAdapter()

        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), RecyclerView.VERTICAL,
                false
            )
            adapter = transactionsAdapter
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
                viewModel.transactionState.collect { state ->
                    when (state) {
                        is TransactionState.Error -> {
                            binding.errorContent.show()
                            binding.errorContent.setOnRetryClickListener {
                                viewModel.loadTransactions()
                            }

                            binding.contentSuccess.hide()
                            binding.progressBar.hide()
                            binding.tvEmptyTransactions.hide()
                        }

                        is TransactionState.Loading -> {
                            binding.progressBar.show()
                            binding.errorContent.hide()
                            binding.contentSuccess.hide()
                            binding.tvEmptyTransactions.hide()

                        }

                        is TransactionState.Success -> {
                            binding.contentSuccess.show()
                            binding.errorContent.hide()
                            binding.progressBar.hide()
                            binding.errorContent.hide()
                            binding.tvEmptyTransactions.hide()

                            transactionsAdapter.submitList(state.transactions)
                        }

                        TransactionState.Empty -> {
                            binding.contentSuccess.show()
                            binding.fabBack.show()
                            binding.tvEmptyTransactions.show()
                            binding.rvTransactions.hide()
                            binding.errorContent.hide()
                            binding.progressBar.hide()

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
                        is UiTransactionEvent.ShowSnackbar -> showSnackbar(event.message)
                    }
                }
            }
        }
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "TransactionsFragment"
    }
}