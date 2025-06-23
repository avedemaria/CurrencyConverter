package com.example.currencyconverter.ui.screens.currenciesEditScreen

import androidx.lifecycle.SavedStateHandle
import com.example.currencyconverter.domain.entity.Account
import com.example.currencyconverter.domain.entity.Currency
import com.example.currencyconverter.domain.entity.CurrencyItem
import com.example.currencyconverter.domain.usecases.AccountUseCase
import com.example.currencyconverter.domain.usecases.GetRatesUseCase
import com.example.currencyconverter.ui.CurrencyUiModel
import com.example.currencyconverter.ui.mapper.CurrencyUiMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test


@ExperimentalCoroutinesApi
class CurrencyEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()


    private lateinit var viewModel: CurrencyEditViewModel
    private lateinit var currencyItems: List<CurrencyItem>
    private lateinit var currencyUiModels: List<CurrencyUiModel>

    private val getRatesUseCase: GetRatesUseCase = mockk()
    private val accountUseCase: AccountUseCase = mockk()
    private val uiMapper: CurrencyUiMapper = mockk()
    val mockAccount = Account(code = Currency.USD, balance = 0.0)
    private lateinit var savedStateHandle: SavedStateHandle

    private val selectedCurrency = CurrencyUiModel(
        currencyCode = Currency.USD.name,
        currencyName = "Us dollar",
        symbol = "$",
        drawableId = 1,
        amount = 1.0,
        balance = 1000.0,
        isSelected = true,
        rateValue = 1.0
    )


    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        savedStateHandle = SavedStateHandle(mapOf("Currency" to selectedCurrency))


        currencyItems = listOf(
            CurrencyItem(Currency.USD, "$", "Us dollar", 1.0, mockAccount),
            CurrencyItem(Currency.EUR, "@", "Euro", 0.86, mockAccount),
        )

        currencyUiModels = currencyItems.map {
            CurrencyUiModel(
                currencyCode = it.code.name,
                amount = 0.0,
                rateValue = it.rateValue,
                currencyName = it.code.name,
                drawableId = 1,
                isSelected = it.code == Currency.USD,
                symbol = it.symbol,
                balance = 1000.0
            )
        }

    }


    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `WHEN loadInitialData is success THEN emit Success state`() = runTest {

        val accountList = listOf(Account(code = Currency.USD, balance = 1000.0))

        coEvery { accountUseCase.getAccountsFromRoom() } returns flowOf(accountList)
        coEvery { getRatesUseCase(any(), any()) } returns currencyItems
        every { uiMapper.currencyEntityToCurrencyUi(any(), any()) } answers {
            val entity = firstArg<CurrencyItem>()
            val isSelected = secondArg<Boolean>()
            currencyUiModels.first() { it.currencyCode == entity.code.name }
                .copy(isSelected = isSelected)
        }

        viewModel = CurrencyEditViewModel(
            getRatesUseCase = getRatesUseCase,
            accountUseCase = accountUseCase,
            uiMapper = uiMapper,
            savedStateHandle = savedStateHandle
        )


        advanceUntilIdle()

        coVerify { accountUseCase.getAccountsFromRoom() }
        coVerify { getRatesUseCase(any(), any()) }
        val state = viewModel.currencyListState.value
        assertTrue(state is CurrencyEditState.Success)
    }


    @Test
    fun `WHEN accountUseCase fails THEN getRatesUseCase is not called`() = runTest {
        coEvery { accountUseCase.getAccountsFromRoom() } throws RuntimeException("DB error")

        viewModel = CurrencyEditViewModel(
            getRatesUseCase = getRatesUseCase,
            accountUseCase = accountUseCase,
            uiMapper = uiMapper,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { accountUseCase.getAccountsFromRoom() }
        coVerify(exactly = 0) { getRatesUseCase(any(), any()) }

        val state = viewModel.currencyListState.value
        assertTrue(state is CurrencyEditState.Error)
    }

    @Test
    fun `WHEN getRatesUseCase fails THEN emit Error state`() = runTest {
        val accountList = listOf(Account(code = Currency.USD, balance = 1000.0))

        coEvery { accountUseCase.getAccountsFromRoom() } returns flowOf(accountList)
        coEvery { getRatesUseCase.invoke(any(), any()) } throws RuntimeException("Rates failed")


        viewModel = CurrencyEditViewModel(
            getRatesUseCase = getRatesUseCase,
            accountUseCase = accountUseCase,
            uiMapper = uiMapper,
            savedStateHandle = savedStateHandle
        )

        advanceUntilIdle()

        coVerify(exactly = 1) { accountUseCase.getAccountsFromRoom() }
        coVerify(exactly = 1) { getRatesUseCase.invoke(any(), any()) }

        val state = viewModel.currencyListState.value
        assertTrue(state is CurrencyEditState.Error)
    }


    @Test
    fun `WHEN refreshCurrenciesAndFilter updates state THEN currencies are filtered by balance`() = runTest {

        val accounts = listOf(
            Account(Currency.USD, 1000.0),
            Account(Currency.EUR, 0.0),
            Account(Currency.RUB, 200.0),
        )
        coEvery { accountUseCase.getAccountsFromRoom() } returns flowOf(accounts)

        val currency = currencyUiModels.first()

        val rates = listOf(
            CurrencyItem(Currency.USD, "$", "US Dollar", 1.0, mockAccount),
            CurrencyItem(Currency.EUR, "€", "Euro", 0.9, mockAccount),
            CurrencyItem(Currency.RUB, "руб.", "Rub", 68.0, mockAccount),
        )
        coEvery { getRatesUseCase(currency.currencyCode, 1.0) } returns rates

        every { uiMapper.currencyEntityToCurrencyUi(any(), any()) } answers {
            val currencyItem = firstArg<CurrencyItem>()
            val isSelected = secondArg<Boolean>()
            CurrencyUiModel(
                currencyCode = currencyItem.code.name,
                currencyName = currencyItem.code.name,
                symbol = currencyItem.symbol,
                drawableId = 0,
                amount = 0.0,
                balance = accounts.find { it.code.name == currencyItem.code.name }?.balance ?: 0.0,
                isSelected = isSelected,
                rateValue = currencyItem.rateValue
            )
        }

        viewModel = CurrencyEditViewModel(
            getRatesUseCase = getRatesUseCase,
            accountUseCase = accountUseCase,
            uiMapper = uiMapper,
            savedStateHandle = savedStateHandle
        )

        viewModel.loadInitialData()

        advanceUntilIdle()

        val state = viewModel.currencyListState.value
        assertTrue(state is CurrencyEditState.Success)
        if (state is CurrencyEditState.Success) {
            val filteredCodes = state.currencies.map { it.currencyCode }
            assertTrue(filteredCodes.contains("USD"))
            assertFalse(filteredCodes.contains("EUR"))
            assertTrue(filteredCodes.contains("RUB"))
        }
    }



}