package com.RichardLiu.notionaccounting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.RichardLiu.notionaccounting.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val notionService: NotionService,
    private val settings: Settings
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val dailyExpenses: Double,
            val weeklyExpenses: Double,
            val monthlyExpenses: Double,
            val currentMonth: YearMonth = YearMonth.now(),
            val dailyExpensesMap: Map<LocalDate, Double> = emptyMap()
        ) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                // 获取今日支出
                val today = LocalDateTime.now()
                val todayStr = today.format(DateTimeFormatter.ISO_DATE)
                Timber.d("Querying for date: $todayStr")
                
                val dailyResponse = notionService.getTransactions(
                    settings.databaseId,
                    QueryRequest(
                        filter = Filter(
                            property = "Real date",
                            formula = Formula(
                                type = "date",
                                date = DateProperty(
                                    equals = todayStr
                                )
                            )
                        )
                    )
                )

                val dailyExpenses = if (dailyResponse.isSuccessful) {
                    dailyResponse.body()?.results?.sumOf { page ->
                        page.properties?.get("Amount")?.number ?: 0.0
                    } ?: 0.0
                } else {
                    Timber.e("Daily query failed: ${dailyResponse.errorBody()?.string()}")
                    0.0
                }

                // 获取本周支出
                val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                val weekEnd = weekStart.plusDays(6)
                val weekFormat = DateTimeFormatter.ofPattern("yy-MM-dd")
                val weekString = "${weekStart.format(weekFormat)} ~ ${weekEnd.format(weekFormat)}"
                Timber.d("Querying for week: $weekString")
                
                val weeklyResponse = notionService.getTransactions(
                    settings.databaseId,
                    QueryRequest(
                        filter = Filter(
                            property = "Week",
                            formula = Formula(
                                type = "string",
                                string = StringFilter(
                                    equals = weekString
                                )
                            )
                        )
                    )
                )

                val weeklyExpenses = if (weeklyResponse.isSuccessful) {
                    weeklyResponse.body()?.results?.sumOf { page ->
                        page.properties?.get("Amount")?.number ?: 0.0
                    } ?: 0.0
                } else {
                    Timber.e("Weekly query failed: ${weeklyResponse.errorBody()?.string()}")
                    0.0
                }

                // 获取本月支出
                val currentMonth = YearMonth.now()
                val monthString = currentMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                Timber.d("Querying for month: $monthString")
                
                val monthlyResponse = notionService.getTransactions(
                    settings.databaseId,
                    QueryRequest(
                        filter = Filter(
                            property = "Month",
                            formula = Formula(
                                type = "string",
                                string = StringFilter(
                                    equals = monthString
                                )
                            )
                        )
                    )
                )

                val monthlyExpenses = if (monthlyResponse.isSuccessful) {
                    monthlyResponse.body()?.results?.sumOf { page ->
                        page.properties?.get("Amount")?.number ?: 0.0
                    } ?: 0.0
                } else {
                    Timber.e("Monthly query failed: ${monthlyResponse.errorBody()?.string()}")
                    0.0
                }

                // 构建每日支出映射
                val dailyExpensesMap = monthlyResponse.body()?.results?.fold(mutableMapOf<LocalDate, Double>()) { map, page ->
                    val date = page.properties?.get("Real date")?.formula?.date?.start?.let { 
                        LocalDate.parse(it.split("T")[0]) 
                    }
                    val amount = page.properties?.get("Amount")?.number ?: 0.0
                    if (date != null) {
                        map[date] = (map[date] ?: 0.0) + amount
                    }
                    map
                } ?: emptyMap()

                _uiState.value = UiState.Success(
                    dailyExpenses = dailyExpenses,
                    weeklyExpenses = weeklyExpenses,
                    monthlyExpenses = monthlyExpenses,
                    currentMonth = currentMonth,
                    dailyExpensesMap = dailyExpensesMap
                )
            } catch (e: Exception) {
                Timber.e(e)
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
} 