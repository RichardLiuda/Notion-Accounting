package com.RichardLiu.notionaccounting.data

import com.RichardLiu.notionaccounting.model.Transaction
import retrofit2.Response
import retrofit2.http.*

interface NotionService {
    @POST("v1/pages")
    suspend fun createPage(@Body page: NotionPage): Response<NotionPage>

    @PATCH("v1/pages/{page_id}")
    suspend fun updatePage(
        @Path("page_id") pageId: String,
        @Body page: NotionPage
    ): Response<NotionPage>

    @PATCH("v1/pages/{page_id}")
    suspend fun deletePage(
        @Path("page_id") pageId: String,
        @Body page: NotionPage = NotionPage(
            parent = Parent(database_id = ""),
            properties = emptyMap(),
            archived = true
        )
    ): Response<NotionPage>

    @POST("v1/databases/{database_id}/query")
    suspend fun getTransactions(
        @Path("database_id") databaseId: String,
        @Body query: QueryRequest = QueryRequest()
    ): Response<NotionResponse>

    @POST("v1/databases/{database_id}/query")
    suspend fun querySummaryPages(
        @Path("database_id") databaseId: String,
        @Body query: QueryRequest
    ): Response<NotionResponse>
}

data class QueryRequest(
    val sorts: List<Sort>? = null,
    val page_size: Int = 100,
    val filter: Filter? = null
)

data class Sort(
    val property: String,
    val direction: String
)

data class Filter(
    val property: String,
    val rich_text: TextFilter? = null,
    val formula: FormulaFilter? = null,
    val title: TitleFilter? = null
)

data class TitleFilter(
    val equals: String
)

data class TextFilter(
    val equals: String? = null,
    val contains: String? = null
)

data class FormulaFilter(
    val string: StringFilter? = null
)

data class StringFilter(
    val equals: String
) 