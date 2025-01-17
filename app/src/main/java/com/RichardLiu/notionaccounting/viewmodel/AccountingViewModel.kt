package com.RichardLiu.notionaccounting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.RichardLiu.notionaccounting.data.TransactionRepository
import com.RichardLiu.notionaccounting.model.Transaction
import com.RichardLiu.notionaccounting.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AccountingViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Transaction>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Transaction>>> = _uiState.asStateFlow()

    init {
        Timber.d("AccountingViewModel initialized")
        loadTransactions()
    }

    private fun loadTransactions() {
        Timber.d("Loading transactions...")
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                repository.getTransactions().collect { transactions ->
                    Timber.d("Received ${transactions.size} transactions")
                    _uiState.value = UiState.Success(transactions)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading transactions")
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addTransaction(transaction: Transaction) {
        Timber.d("Adding new transaction: $transaction")
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.addTransaction(transaction)
                Timber.d("Transaction added successfully")
                loadTransactions()
            } catch (e: Exception) {
                Timber.e(e, "Error adding transaction")
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteTransaction(pageId: String) {
        Timber.d("Deleting transaction: $pageId")
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.deleteTransaction(pageId)
                Timber.d("Transaction deleted successfully")
                loadTransactions()
            } catch (e: Exception) {
                Timber.e(e, "Error deleting transaction")
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
} 