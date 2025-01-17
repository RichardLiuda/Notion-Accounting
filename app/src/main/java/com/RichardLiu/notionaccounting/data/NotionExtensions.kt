package com.RichardLiu.notionaccounting.data

import com.RichardLiu.notionaccounting.model.Transaction
import com.RichardLiu.notionaccounting.BuildConfig
import com.RichardLiu.notionaccounting.model.TransactionType
import com.RichardLiu.notionaccounting.model.TransactionCategory
import timber.log.Timber

data class NotionPage(
    val parent: Parent,
    val properties: Map<String, Property>
)

data class Parent(
    val database_id: String
)

data class Property(
    val type: String,
    val title: List<TextContent>? = null,
    val rich_text: List<TextContent>? = null,
    val number: Double? = null,
    val select: Select? = null,
    val date: DateProperty? = null,
    val formula: Formula? = null
)

data class Formula(
    val type: String,
    val date: DateProperty? = null
)

data class DateProperty(
    val start: String?,
    val end: String? = null,
    val time_zone: String? = null
)

data class TextContent(
    val text: Text
)

data class Text(
    val content: String
)

data class Select(
    val name: String
)

fun Transaction.toNotionPage(): NotionPage {
    val databaseId = BuildConfig.NOTION_DATABASE_ID
    Timber.d("Using database ID: $databaseId")
    
    if (databaseId.isBlank()) {
        Timber.e("Database ID is empty! Please check your local.properties configuration.")
    }
    
    val page = NotionPage(
        parent = Parent(database_id = databaseId),
        properties = mapOf(
            "Reminder" to Property(
                type = "title",
                title = listOf(TextContent(Text(content = category.displayName)))
            ),
            "Amount" to Property(
                type = "number",
                number = amount
            ),
            "Tags" to Property(
                type = "select",
                select = Select(name = category.notionName)
            ),
            "Description" to Property(
                type = "rich_text",
                rich_text = listOf(TextContent(Text(content = description)))
            )
        )
    )
    
    Timber.d("Created NotionPage: $page")
    return page
}

data class NotionResponse(
    val results: List<NotionPage>
)

fun NotionResponse.parseToTransactions(): List<Transaction> {
    return results.map { page ->
        val tagName = page.properties["Tags"]?.select?.name
        val category = TransactionCategory.values().find { it.notionName == tagName }
            ?: TransactionCategory.DAILY

        val description = page.properties["Description"]?.rich_text?.firstOrNull()?.text?.content ?: ""
        val reminder = page.properties["Reminder"]?.title?.firstOrNull()?.text?.content ?: ""
        val finalDescription = if (description.isNotEmpty()) description else reminder

        val dateProperty = page.properties["Real date"]
        Timber.d("Date property: $dateProperty")
        val dateValue = dateProperty?.formula?.date?.start
        Timber.d("Date value: $dateValue")

        Transaction(
            description = finalDescription,
            amount = page.properties["Amount"]?.number ?: 0.0,
            type = TransactionType.EXPENSE,
            category = category,
            note = "",
            date = dateValue ?: ""
        )
    }
} 