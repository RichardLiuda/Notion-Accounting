package com.RichardLiu.notionaccounting.data

data class NotionResponse(
    val results: List<NotionPage> = emptyList(),
    val next_cursor: String? = null,
    val has_more: Boolean = false
)

data class QueryRequest(
    val filter: Filter? = null,
    val sorts: List<Sort>? = null,
    val page_size: Int = 100
)

data class Sort(
    val property: String,
    val direction: String = "descending"
)

data class Filter(
    val property: String,
    val formula: Formula? = null,
    val rollup: Rollup? = null,
    val rich_text: TextFilter? = null,
    val title: TitleFilter? = null
)

data class TitleFilter(
    val equals: String
)

data class TextFilter(
    val equals: String? = null,
    val contains: String? = null
)

data class Formula(
    val type: String? = null,
    val string: StringFilter? = null,
    val date: DateProperty? = null
)

data class FormulaValue(
    val type: String? = null,
    val string: String? = null,
    val date: DateProperty? = null
)

data class StringFilter(
    val equals: String
)

data class Rollup(
    val function: String,
    val number: Double? = null
) 