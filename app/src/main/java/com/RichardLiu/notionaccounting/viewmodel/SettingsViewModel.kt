package com.RichardLiu.notionaccounting.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.RichardLiu.notionaccounting.BuildConfig
import com.RichardLiu.notionaccounting.data.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

data class SettingsState(
    val apiKey: String = "",
    val databaseId: String = "",
    val monthSummaryDatabaseId: String = "",
    val weekSummaryDatabaseId: String = "",
    val error: String = ""
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: Settings
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        try {
            _uiState.value = SettingsState(
                apiKey = settings.apiKey,
                databaseId = settings.databaseId,
                monthSummaryDatabaseId = settings.monthSummaryDatabaseId,
                weekSummaryDatabaseId = settings.weekSummaryDatabaseId
            )
        } catch (e: Exception) {
            Timber.e(e, "Error loading settings")
            _uiState.value = _uiState.value.copy(error = "加载设置失败：${e.message}")
        }
    }

    fun updateApiKey(value: String) {
        _uiState.value = _uiState.value.copy(apiKey = value)
    }

    fun updateDatabaseId(value: String) {
        _uiState.value = _uiState.value.copy(databaseId = value)
    }

    fun updateMonthSummaryDatabaseId(value: String) {
        _uiState.value = _uiState.value.copy(monthSummaryDatabaseId = value)
    }

    fun updateWeekSummaryDatabaseId(value: String) {
        _uiState.value = _uiState.value.copy(weekSummaryDatabaseId = value)
    }

    fun saveSettings() {
        try {
            with(uiState.value) {
                settings.updateSettings(
                    apiKey = apiKey,
                    databaseId = databaseId,
                    monthSummaryDatabaseId = monthSummaryDatabaseId,
                    weekSummaryDatabaseId = weekSummaryDatabaseId
                )
            }
            _uiState.value = _uiState.value.copy(error = "")
        } catch (e: Exception) {
            Timber.e(e, "Error saving settings")
            _uiState.value = _uiState.value.copy(error = "保存设置失败：${e.message}")
        }
    }
} 