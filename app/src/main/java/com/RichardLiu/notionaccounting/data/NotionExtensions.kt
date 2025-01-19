package com.RichardLiu.notionaccounting.data

import com.RichardLiu.notionaccounting.model.Transaction
import com.RichardLiu.notionaccounting.BuildConfig
import com.RichardLiu.notionaccounting.model.TransactionType
import com.RichardLiu.notionaccounting.model.TransactionCategory
import timber.log.Timber

data class NotionPage(
    val id: String? = null,
    val parent: Parent,
    val properties: Map<String, Property>? = null,
    val archived: Boolean? = null
)

data class Parent(
    val database_id: String,
    val type: String = "database_id"
)

data class Property(
    val type: String,
    val title: List<TextContent>? = null,
    val rich_text: List<TextContent>? = null,
    val number: Double? = null,
    val select: Select? = null,
    val date: DateProperty? = null,
    val formula: FormulaValue? = null,
    val relation: List<Relation>? = null,
    val rollup: Rollup? = null
)

data class Relation(
    val id: String
)

data class DateProperty(
    val start: String? = null,
    val end: String? = null,
    val equals: String? = null,
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

data class SummaryInfo(
    val month: String,
    val week: String,
    val monthPageId: String,
    val weekPageId: String
)

fun Transaction.toNotionPage(): NotionPage {
    val databaseId = BuildConfig.NOTION_DATABASE_ID
    Timber.d("[NOTION_PAGE] Using database ID: $databaseId")
    
    if (databaseId.isBlank()) {
        Timber.e("[NOTION_ERROR] Database ID is empty! Please check your local.properties configuration.")
    }
    
    val displayText = if (note.isNotEmpty()) {
        note
    } else {
        ""
    }
    
    val properties = mapOf(
        "Reminder" to Property(
            type = "title",
            title = listOf(TextContent(Text(content = displayText)))
        ),
        "Amount" to Property(
            type = "number",
            number = amount
        ),
        "Tags" to Property(
            type = "select",
            select = Select(name = category.notionName)
        )
    )
    
    Timber.d("[NOTION_PAGE] Creating page with properties: $properties")
    
    val page = NotionPage(
        parent = Parent(database_id = databaseId),
        properties = properties
    )
    
    Timber.d("[NOTION_PAGE] Created NotionPage: $page")
    return page
}

fun NotionResponse.parseToTransactions(): List<Transaction> {
    return results.map { page ->
        val tagName = page.properties?.get("Tags")?.select?.name
        val category = TransactionCategory.values().find { it.notionName == tagName }
            ?: TransactionCategory.DAILY

        val description = page.properties?.get("Description")?.rich_text?.firstOrNull()?.text?.content ?: ""
        val reminder = page.properties?.get("Reminder")?.title?.firstOrNull()?.text?.content ?: ""
        val finalDescription = if (description.isNotEmpty()) description else reminder

        val dateProperty = page.properties?.get("Real date")
        Timber.d("Date property: $dateProperty")
        val dateValue = dateProperty?.formula?.date?.start
        Timber.d("Date value: $dateValue")

        Transaction(
            description = finalDescription,
            amount = page.properties?.get("Amount")?.number ?: 0.0,
            type = TransactionType.EXPENSE,
            category = category,
            note = "",
            date = dateValue ?: "",
            pageId = page.id ?: ""
        )
    }
} 