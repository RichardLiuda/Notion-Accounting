package com.RichardLiu.notionaccounting.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.RichardLiu.notionaccounting.model.Transaction
import com.RichardLiu.notionaccounting.model.TransactionType
import com.RichardLiu.notionaccounting.viewmodel.AccountingViewModel
import timber.log.Timber

@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            when (uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    val transactions = (uiState as UiState.Success<List<Transaction>>).data
                    TransactionList(
                        transactions = transactions,
                        modifier = Modifier.weight(1f),
                        onDelete = { pageId -> showDeleteConfirmDialog = pageId }
                    )
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (uiState as UiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
                TextButton(
                    onClick = { showDeleteConfirmDialog = null }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

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
        items(transactions) { transaction ->
            TransactionItem(
                transaction = transaction,
                onDelete = onDelete
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    onDelete: (String) -> Unit = {}
) {
    val dateTimeText = if (transaction.date.isNotEmpty()) {
        val parts = transaction.date.split("T")
        if (parts.size == 2) {
            val date = parts[0]
            val time = parts[1].substringBefore(".")  // 移除毫秒部分
            "$date ${time.substring(0, 5)}"  // 只显示小时和分钟
        } else {
            transaction.date
        }
    } else {
        "No date"
    }
    
    val amountText = if (transaction.type == TransactionType.INCOME) 
        "+${String.format("%.2f", transaction.amount)}" 
    else 
        "-${String.format("%.2f", transaction.amount)}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = amountText,
                        color = if (transaction.type == TransactionType.INCOME)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = dateTimeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (transaction.pageId.isNotEmpty()) {
                    IconButton(
                        onClick = { onDelete(transaction.pageId) }
                    ) {
                        Text("×", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
} 