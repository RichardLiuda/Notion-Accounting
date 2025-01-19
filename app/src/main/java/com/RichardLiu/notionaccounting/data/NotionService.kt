package com.RichardLiu.notionaccounting.data

import retrofit2.Response
import retrofit2.http.*

interface NotionService {
    @POST("databases/{database_id}/query")
    suspend fun getTransactions(
        @Path("database_id") databaseId: String,
        @Body query: QueryRequest
    ): Response<NotionResponse>

    @POST("databases/{database_id}/query")
    suspend fun querySummaryPages(
        @Path("database_id") databaseId: String,
        @Body query: QueryRequest
    ): Response<NotionResponse>

    @POST("pages")
    suspend fun createPage(
        @Body page: NotionPage
    ): Response<NotionPage>

    @PATCH("pages/{page_id}")
    suspend fun updatePage(
        @Path("page_id") pageId: String,
        @Body page: NotionPage
    ): Response<NotionPage>

    @PATCH("pages/{page_id}")
    suspend fun deletePage(
        @Path("page_id") pageId: String,
        @Body page: NotionPage
    ): Response<NotionPage>
} 