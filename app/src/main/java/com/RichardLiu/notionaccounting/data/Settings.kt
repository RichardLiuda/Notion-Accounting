package com.RichardLiu.notionaccounting.data

import android.content.Context
import android.content.SharedPreferences
import com.RichardLiu.notionaccounting.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Settings @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("notion_settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString("NOTION_API_KEY", BuildConfig.NOTION_API_KEY) ?: BuildConfig.NOTION_API_KEY
        private set(value) = prefs.edit().putString("NOTION_API_KEY", value).apply()

    var databaseId: String
        get() = prefs.getString("NOTION_DATABASE_ID", BuildConfig.NOTION_DATABASE_ID) ?: BuildConfig.NOTION_DATABASE_ID
        private set(value) = prefs.edit().putString("NOTION_DATABASE_ID", value).apply()

    var monthSummaryDatabaseId: String
        get() = prefs.getString("NOTION_MONTH_SUMMARY_DATABASE_ID", BuildConfig.NOTION_MONTH_SUMMARY_DATABASE_ID) ?: BuildConfig.NOTION_MONTH_SUMMARY_DATABASE_ID
        private set(value) = prefs.edit().putString("NOTION_MONTH_SUMMARY_DATABASE_ID", value).apply()

    var weekSummaryDatabaseId: String
        get() = prefs.getString("NOTION_WEEK_SUMMARY_DATABASE_ID", BuildConfig.NOTION_WEEK_SUMMARY_DATABASE_ID) ?: BuildConfig.NOTION_WEEK_SUMMARY_DATABASE_ID
        private set(value) = prefs.edit().putString("NOTION_WEEK_SUMMARY_DATABASE_ID", value).apply()

    fun updateSettings(
        apiKey: String,
        databaseId: String,
        monthSummaryDatabaseId: String,
        weekSummaryDatabaseId: String
    ) {
        this.apiKey = apiKey
        this.databaseId = databaseId
        this.monthSummaryDatabaseId = monthSummaryDatabaseId
        this.weekSummaryDatabaseId = weekSummaryDatabaseId
    }
} 