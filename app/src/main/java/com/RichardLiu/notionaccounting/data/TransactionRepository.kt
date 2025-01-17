package com.RichardLiu.notionaccounting.data

import com.RichardLiu.notionaccounting.BuildConfig
import com.RichardLiu.notionaccounting.model.Transaction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.net.SocketTimeoutException
import javax.inject.Inject

class TransactionRepository @Inject constructor(
    private val notionService: NotionService
) {
    private val databaseId = BuildConfig.NOTION_DATABASE_ID
    private val maxRetries = 3
    private val initialRetryDelay = 1000L // 1 second

    init {
        Timber.d("TransactionRepository initialized with database ID: $databaseId")
    }

    private suspend fun <T> retryOnTimeout(
        maxAttempts: Int = maxRetries,
        initialDelay: Long = initialRetryDelay,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxAttempts - 1) { attempt ->
            try {
                return block()
            } catch (e: SocketTimeoutException) {
                Timber.w("Attempt ${attempt + 1} failed with timeout, retrying after $currentDelay ms")
                delay(currentDelay)
                currentDelay *= 2 // 指数退避
            }
        }
        return block() // 最后一次尝试
    }

    suspend fun addTransaction(transaction: Transaction) {
        Timber.d("Creating Notion page for transaction: $transaction")
        try {
            retryOnTimeout {
                notionService.createPage(transaction.toNotionPage())
                Timber.d("Successfully created Notion page for transaction")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to create Notion page for transaction after $maxRetries attempts")
            throw e
        }
    }

    fun getTransactions(): Flow<List<Transaction>> = flow {
        Timber.d("Fetching transactions from Notion database")
        try {
            val query = QueryRequest(
                sorts = listOf(Sort(property = "Real date", direction = "descending")),
                page_size = 100
            )
            
            val response = retryOnTimeout {
                notionService.getTransactions(databaseId, query)
            }
            if (response.isSuccessful) {
                val transactions = response.body()?.parseToTransactions() ?: emptyList()
                Timber.d("Successfully fetched ${transactions.size} transactions from Notion")
                emit(transactions)
            } else {
                Timber.w("Failed to fetch transactions: ${response.code()} - ${response.message()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching transactions from Notion after $maxRetries attempts")
            emit(emptyList())
        }
    }
} 