package com.RichardLiu.notionaccounting.model

data class Transaction(
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val note: String = "",
    val date: String = ""
) 