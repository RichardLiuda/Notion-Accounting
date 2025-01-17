package com.RichardLiu.notionaccounting.model

enum class TransactionCategory(val displayName: String, val notionName: String) {
    DAILY("日用", "日用"),
    DIGITAL("数码", "数码"),
    STUDY("学习", "学习"),
    ENTERTAINMENT("娱乐", "娱乐"),
    TRANSPORT("交通", "交通"),
    SNACKS("零食", "零食"),
    TRAVEL("旅行", "旅行"),
    FOOD("吃饭", "吃饭"),
    MILLET("谷子", "谷子"),
    MEDICAL("医疗", "医疗");

    override fun toString(): String {
        return displayName
    }
} 