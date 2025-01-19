package com.RichardLiu.notionaccounting.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.RichardLiu.notionaccounting.model.Transaction
import com.RichardLiu.notionaccounting.model.TransactionType
import com.RichardLiu.notionaccounting.model.TransactionCategory
import com.RichardLiu.notionaccounting.viewmodel.AccountingViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AccountingViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.FOOD) }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    val backgroundColor = animateColorAsState(
        if (selectedType == TransactionType.EXPENSE)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
        else
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 交易类型选择
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.value
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TransactionType.values().forEach { type ->
                    val isSelected = selectedType == type
                    val textColor = animateColorAsState(
                        if (isSelected) {
                            if (type == TransactionType.EXPENSE)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        FilledTonalIconButton(
                            onClick = { selectedType = type },
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = if (isSelected) {
                                    if (type == TransactionType.EXPENSE)
                                        MaterialTheme.colorScheme.errorContainer
                                    else
                                        MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Icon(
                                imageVector = if (type == TransactionType.EXPENSE) 
                                    Icons.Default.Close
                                else 
                                    Icons.Default.Add,
                                contentDescription = null,
                                tint = textColor.value
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (type == TransactionType.EXPENSE) "支出" else "收入",
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor.value
                        )
                    }
                }
            }
        }

        // 金额输入
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = MaterialTheme.shapes.medium
                )

                // 分类选择
                Box {
                    TextField(
                        value = selectedCategory.displayName,
                        onValueChange = { },
                        label = { Text("分类") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryDropdown = true },
                        readOnly = true,
                        enabled = false,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "选择分类",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                    
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .width(240.dp)
                    ) {
                        TransactionCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (selectedCategory == category)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryDropdown = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = if (selectedCategory == category)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.height(48.dp)
                            )
                        }
                    }
                }

                // 备注输入
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 提交按钮
        Button(
            onClick = {
                amount.toDoubleOrNull()?.let { amountValue ->
                    viewModel.addTransaction(
                        transaction = Transaction(
                            amount = amountValue,
                            description = description,
                            type = selectedType,
                            category = selectedCategory,
                            date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        )
                    )
                    // 清空输入
                    amount = ""
                    description = ""
                    selectedCategory = TransactionCategory.FOOD
                    selectedType = TransactionType.EXPENSE
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = amount.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedType == TransactionType.EXPENSE)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                if (selectedType == TransactionType.EXPENSE) "记一笔支出" else "记一笔收入", 
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
} 