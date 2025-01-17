package com.RichardLiu.notionaccounting.data

import com.RichardLiu.notionaccounting.model.Transaction
import retrofit2.Response
import retrofit2.http.*

interface NotionService {
    @POST("v1/pages")
    suspend fun createPage(@Body page: NotionPage): Response<NotionPage>

    @POST("v1/databases/{database_id}/query")
    suspend fun getTransactions(
        @Path("database_id") databaseId: String,
        @Body query: QueryRequest = QueryRequest()
    ): Response<NotionResponse>
}

data class QueryRequest(
    val sorts: List<Sort> = listOf(
        Sort(property = "Real date", direction = "descending")
    ),
    val page_size: Int = 100
)

data class Sort(
    val property: String,
    val direction: String
) 