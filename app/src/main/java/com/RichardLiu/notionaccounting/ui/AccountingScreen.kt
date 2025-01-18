package com.RichardLiu.notionaccounting.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissValue
import androidx.compose.material.DismissDirection
import androidx.compose.material.rememberDismissState
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.swipeable
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.gestures.Orientation
import androidx.hilt.navigation.compose.hiltViewModel
import com.RichardLiu.notionaccounting.model.Transaction
import com.RichardLiu.notionaccounting.model.TransactionType
import com.RichardLiu.notionaccounting.viewmodel.AccountingViewModel
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var currentTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            currentTransactions = (uiState as UiState.Success<List<Transaction>>).data
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            delay(300) // 给动画一些时间
            viewModel.loadTransactions()
            isRefreshing = false
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { isRefreshing = true }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (currentTransactions.isEmpty() && !isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无记录",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    TransactionList(
                        transactions = currentTransactions,
                        onDelete = { pageId -> showDeleteConfirmDialog = pageId }
                    )
                }
            }

            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("添加交易")
            }
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            scale = true
        )
    }

    if (showDialog) {
        AddTransactionDialog(
            onDismiss = { showDialog = false },
            onConfirm = { transaction ->
                viewModel.addTransaction(transaction)
                showDialog = false
            }
        )
    }

    showDeleteConfirmDialog?.let { pageId ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(pageId)
                        showDeleteConfirmDialog = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionList(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    onDelete: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = transactions,
            key = { it.pageId.ifEmpty { it.hashCode().toString() } }
        ) { transaction ->
            TransactionItem(
                transaction = transaction,
                onDelete = onDelete,
                modifier = Modifier
                    .animateItemPlacement(
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = LinearOutSlowInEasing
                        )
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(150)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearOutSlowInEasing
            )
        ) + slideInHorizontally(
            animationSpec = tween(
                durationMillis = 500,
                easing = LinearOutSlowInEasing
            ),
            initialOffsetX = { -it }
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        ) + slideOutHorizontally(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            ),
            targetOffsetX = { -it }
        )
    ) {
        val deleteButtonWidth = 120.dp
        val swipeableState = rememberSwipeableState(initialValue = 0)
        val density = LocalDensity.current
        val anchors = with(density) {
            mapOf(
                0f to 0,
                -deleteButtonWidth.toPx() to 1
            )
        }

        Box(
            modifier = modifier
                .padding(vertical = 4.dp)
        ) {
            // 删除按钮
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
            ) {
                FilledTonalButton(
                    onClick = { onDelete(transaction.pageId) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .width(deleteButtonWidth - 32.dp)
                        .height(40.dp)
                ) {
                    Text("删除")
                }
            }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                    .swipeable(
                        state = swipeableState,
                        anchors = anchors,
                        thresholds = { _, _ -> FractionalThreshold(0.3f) },
                        orientation = Orientation.Horizontal
                    ),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(
                    defaultElevation = 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = transaction.category.displayName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (transaction.description.isNotEmpty()) {
                            Text(
                                text = transaction.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(start = 16.dp)
                    ) {
                        Text(
                            text = if (transaction.type == TransactionType.INCOME) 
                                "+${String.format("%.2f", transaction.amount)}" 
                            else 
                                "-${String.format("%.2f", transaction.amount)}",
                            color = if (transaction.type == TransactionType.INCOME)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (transaction.date.isNotEmpty()) {
                                val parts = transaction.date.split("T")
                                if (parts.size == 2) {
                                    val date = parts[0]
                                    val time = parts[1].substringBefore(".")
                                    "$date ${time.substring(0, 5)}"
                                } else {
                                    transaction.date
                                }
                            } else {
                                "No date"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
} 