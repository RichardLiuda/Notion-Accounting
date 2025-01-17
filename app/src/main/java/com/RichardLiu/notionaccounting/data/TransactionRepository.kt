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
    private val monthSummaryDatabaseId = "11869776e204809b845ed62a1ad79276"
    private val weekSummaryDatabaseId = "11769776e20481c39130d3417ad5d13c"
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

    private suspend fun findOrCreateSummaryPage(
        databaseId: String,
        propertyName: String,
        propertyValue: String
    ): String {
        // 先查找是否存在
        val query = QueryRequest(
            filter = Filter(
                property = propertyName,
                rich_text = TextFilter(contains = propertyValue)
            ),
            page_size = 1,
            sorts = null  // 明确指定不需要排序
        )

        val response = retryOnTimeout {
            Timber.d("[NOTION_SUMMARY] Querying summary pages with filter: $query")
            val response = notionService.querySummaryPages(databaseId, query)
            Timber.d("[NOTION_SUMMARY] Query response: ${response.code()} ${response.message()}")
            if (!response.isSuccessful) {
                Timber.e("[NOTION_ERROR] Error querying summary pages: ${response.errorBody()?.string()}")
            }
            response
        }

        // 如果找到了，返回页面 ID
        if (response.isSuccessful && !response.body()?.results.isNullOrEmpty()) {
            val pageId = response.body()!!.results[0].id!!
            Timber.d("[NOTION_SUMMARY] Found existing summary page: $pageId")
            return pageId
        }

        // 如果没找到，创建新页面
        val newPage = NotionPage(
            parent = Parent(database_id = databaseId),
            properties = mapOf(
                propertyName to Property(
                    type = "title",
                    title = listOf(TextContent(Text(content = propertyValue)))
                )
            )
        )

        val createResponse = retryOnTimeout {
            Timber.d("[NOTION_SUMMARY] Creating new summary page: $newPage")
            val response = notionService.createPage(newPage)
            Timber.d("[NOTION_SUMMARY] Create response: ${response.code()} ${response.message()}")
            if (!response.isSuccessful) {
                Timber.e("[NOTION_ERROR] Error creating summary page: ${response.errorBody()?.string()}")
            }
            response
        }

        if (!createResponse.isSuccessful) {
            throw Exception("Failed to create summary page: ${createResponse.code()} ${createResponse.message()}")
        }

        val pageId = createResponse.body()!!.id!!
        Timber.d("[NOTION_SUMMARY] Created new summary page: $pageId")
        return pageId
    }

    suspend fun addTransaction(transaction: Transaction) {
        Timber.d("[NOTION_ADD] Creating Notion page for transaction: $transaction")
        try {
            // 先创建交易记录
            val transactionPage = retryOnTimeout {
                val page = transaction.toNotionPage()
                Timber.d("[NOTION_ADD] Sending page to Notion: $page")
                val response = notionService.createPage(page)
                Timber.d("[NOTION_ADD] Notion response: ${response.code()} ${response.message()}")
                if (!response.isSuccessful) {
                    Timber.e("[NOTION_ERROR] Error response body: ${response.errorBody()?.string()}")
                }
                response
            }

            if (!transactionPage.isSuccessful || transactionPage.body() == null) {
                throw Exception("[NOTION_ERROR] Failed to create transaction page: ${transactionPage.code()} ${transactionPage.message()}")
            }

            // 从返回的页面中获取 Month 和 Week 的值
            val month = transactionPage.body()!!.properties?.get("Month")?.formula?.string
                ?: throw Exception("[NOTION_ERROR] Month formula not found")
            val week = transactionPage.body()!!.properties?.get("Week")?.formula?.string
                ?: throw Exception("[NOTION_ERROR] Week formula not found")

            Timber.d("[NOTION_ADD] Got Month: $month, Week: $week")

            // 查找或创建月度和周度总结页面
            val monthPageId = findOrCreateSummaryPage(monthSummaryDatabaseId, "Month", month)
            val weekPageId = findOrCreateSummaryPage(weekSummaryDatabaseId, "Week", week)

            // 更新交易记录，添加关联
            val updatedPage = NotionPage(
                id = transactionPage.body()!!.id,
                parent = Parent(database_id = databaseId),
                properties = mapOf(
                    "月度总结" to Property(
                        type = "relation",
                        relation = listOf(Relation(id = monthPageId))
                    ),
                    "周度总结" to Property(
                        type = "relation",
                        relation = listOf(Relation(id = weekPageId))
                    )
                )
            )

            val updateResponse = retryOnTimeout {
                notionService.updatePage(updatedPage.id!!, updatedPage)
            }

            if (!updateResponse.isSuccessful) {
                throw Exception("Failed to update transaction page with relations: ${updateResponse.code()} ${updateResponse.message()}")
            }

            Timber.d("Successfully created and updated Notion page for transaction")
        } catch (e: Exception) {
            Timber.e(e, "Failed to create Notion page for transaction: ${e.message}")
            throw e
        }
    }

    fun getTransactions(): Flow<List<Transaction>> = flow {
        Timber.d("Fetching transactions from Notion database")
        try {
            val query = QueryRequest(
                sorts = listOf(Sort(property = "Real date", direction = "descending")),
                page_size = 100,
                filter = null
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

    suspend fun deleteTransaction(pageId: String) {
        Timber.d("[NOTION_DELETE] Deleting transaction page: $pageId")
        try {
            val response = retryOnTimeout {
                notionService.deletePage(
                    pageId = pageId,
                    page = NotionPage(
                        parent = Parent(database_id = databaseId),
                        properties = emptyMap(),
                        archived = true
                    )
                )
            }
            
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Timber.e("[NOTION_ERROR] Failed to delete transaction: ${response.code()} ${response.message()}")
                Timber.e("[NOTION_ERROR] Error response body: $errorBody")
                throw Exception("Failed to delete transaction: ${response.code()} ${response.message()}\nError: $errorBody")
            }
            
            Timber.d("[NOTION_DELETE] Successfully deleted transaction page")
        } catch (e: Exception) {
            Timber.e(e, "[NOTION_ERROR] Error deleting transaction page: ${e.message}")
            throw e
        }
    }
} 