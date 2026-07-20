package com.example.paddupushtakam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paddupushtakam.data.TransactionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeletedTransactionsScreen(
    viewModel: TransactionViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val deletedTransactions by viewModel.deletedTransactions.collectAsState()
    
    var selectionMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<TransactionEntity>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (selectionMode) {
                        Text("${selectedItems.size} Selected", color = Color.White)
                    } else {
                        Text("Deleted Transactions", color = Color.White)
                    }
                },
                navigationIcon = {
                    if (selectionMode) {
                        IconButton(onClick = { 
                            selectionMode = false
                            selectedItems.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Selection", tint = Color.White)
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    if (selectionMode && selectedItems.isNotEmpty()) {
                        val tooltipState = rememberTooltipState(isPersistent = true)
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(200)
                            tooltipState.show()
                            kotlinx.coroutines.delay(5000)
                            tooltipState.dismiss()
                        }
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                            tooltip = {
                                PlainTooltip {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowUp,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.padding(end = 12.dp).size(24.dp)
                                        )
                                        Text("Click here to restore the selected deleted item(s)")
                                    }
                                }
                            },
                            state = tooltipState
                        ) {
                            IconButton(onClick = {
                                viewModel.restoreTransactions(selectedItems.toList())
                                selectionMode = false
                                selectedItems.clear()
                            }) {
                                Icon(Icons.Default.Restore, contentDescription = "Restore", tint = Color.White)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0052CC))
            )
        }
    ) { padding ->
        if (deletedTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF0F2F5)), 
                contentAlignment = Alignment.Center
            ) {
                Text("No deleted transactions", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF0F2F5))
            ) {
                items(deletedTransactions) { transaction ->
                    val isChecked = selectedItems.contains(transaction)
                    
                    TransactionItem(
                        transaction = transaction,
                        isDeletedView = true,
                        showCheckbox = selectionMode,
                        isChecked = isChecked,
                        onCheckedChange = { checked ->
                            if (checked) {
                                selectedItems.add(transaction)
                            } else {
                                selectedItems.remove(transaction)
                            }
                            if (selectedItems.isEmpty()) selectionMode = false
                        },
                        onClick = {
                            if (selectionMode) {
                                if (isChecked) {
                                    selectedItems.remove(transaction)
                                } else {
                                    selectedItems.add(transaction)
                                }
                                if (selectedItems.isEmpty()) selectionMode = false
                            }
                        },
                        onLongClick = {
                            if (!selectionMode) {
                                selectionMode = true
                                selectedItems.add(transaction)
                            }
                        }
                    )
                }
            }
        }
    }
}
