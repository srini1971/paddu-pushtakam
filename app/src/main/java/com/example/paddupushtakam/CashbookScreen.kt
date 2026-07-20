package com.example.paddupushtakam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paddupushtakam.data.TransactionEntity
import com.example.paddupushtakam.data.TransactionType
import com.example.paddupushtakam.data.CategoryEntity
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import coil.compose.AsyncImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ArrowDropDown
import android.net.Uri
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.Delete

/**
 * CashbookScreen is the primary user interface of the app.
 * It displays the total balances, today's summary, and a list of all non-deleted transactions.
 * It also handles the "IN" (Add Income) and "OUT" (Add Expense) dialogs.
 * 
 * @param viewModel The ViewModel bridging this UI to the database.
 * @param onNavigateToReport Callback triggered when the user wants to view the report screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashbookScreen(
    viewModel: TransactionViewModel = viewModel(),
    onNavigateToReport: () -> Unit = {},
    onNavigateToDeletedTransactions: () -> Unit = {}
) {
    // These 'collectAsState()' calls listen to the Flow streams in the ViewModel.
    // Whenever data in the database changes, these variables are automatically updated,
    // and Jetpack Compose triggers a re-render of this screen.
    val allTransactions by viewModel.allTransactions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val totalIn by viewModel.totalIn.collectAsState()
    val totalOut by viewModel.totalOut.collectAsState()
    val todayIn by viewModel.todayIn.collectAsState()
    val todayOut by viewModel.todayOut.collectAsState()
    val customFieldsList by viewModel.customFields.collectAsState()
    val deletedTransactions by viewModel.deletedTransactions.collectAsState()
    val products by viewModel.allProducts.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf(TransactionType.IN) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    val totalBalance = totalIn - totalOut
    val todayBalance = todayIn - todayOut
    
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis
    
    val totalCashBalance = allTransactions.filter { it.paymentMode == "Cash" }.sumOf { if (it.type == TransactionType.IN) it.amount else -it.amount }
    val totalOnlineBalance = allTransactions.filter { it.paymentMode != "Cash" }.sumOf { if (it.type == TransactionType.IN) it.amount else -it.amount }
    
    val todayTransactions = allTransactions.filter { it.timestamp >= startOfToday }
    val todayCashBalance = todayTransactions.filter { it.paymentMode == "Cash" }.sumOf { if (it.type == TransactionType.IN) it.amount else -it.amount }
    val todayOnlineBalance = todayTransactions.filter { it.paymentMode != "Cash" }.sumOf { if (it.type == TransactionType.IN) it.amount else -it.amount }
    

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cashbook", color = Color.White) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0052CC))
            )
        },
        bottomBar = {
            BottomActionButtons(
                onOutClick = {
                    transactionToEdit = null
                    dialogType = TransactionType.OUT
                    showAddDialog = true
                },
                onInClick = {
                    transactionToEdit = null
                    dialogType = TransactionType.IN
                    showAddDialog = true
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0F2F5))
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0052CC))
                    .padding(16.dp)
            ) {
                CombinedBalanceCard(
                    modifier = Modifier.fillMaxWidth(),
                    totalBalance = totalBalance,
                    todayBalance = todayBalance,
                    totalCashBalance = totalCashBalance,
                    totalOnlineBalance = totalOnlineBalance,
                    todayCashBalance = todayCashBalance,
                    todayOnlineBalance = todayOnlineBalance,
                    onReportClick = onNavigateToReport,
                    onDeletedTransactionsClick = onNavigateToDeletedTransactions
                )
            }
            // Group transactions by date
            val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
            val groupedTransactions = remember(allTransactions) {
                allTransactions.groupBy { dateFormat.format(Date(it.timestamp)).uppercase() }
            }

            // List Section
            if (allTransactions.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedTransactions.forEach { (dateString, transactionsForDate) ->
                        val inTotal = transactionsForDate.filter { it.type == com.example.paddupushtakam.data.TransactionType.IN }.sumOf { it.amount }
                        val outTotal = transactionsForDate.filter { it.type == com.example.paddupushtakam.data.TransactionType.OUT }.sumOf { it.amount }
                        
                        item {
                            SummaryRow(
                                dateString = dateString,
                                inAmount = inTotal,
                                outAmount = outTotal,
                                numEntries = transactionsForDate.size
                            )
                            HorizontalDivider(color = Color.LightGray)
                        }
                        
                        items(transactionsForDate) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onLongClick = {
                                    transactionToEdit = it
                                    dialogType = it.type
                                    showAddDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
        
        if (showAddDialog) {
            AddTransactionDialog(
                type = dialogType,
                editingTransaction = transactionToEdit,
                categories = categories,
                customFieldsList = customFieldsList,
                products = products,
                onDismiss = { showAddDialog = false },
                onAdd = { amount, desc, category, timestamp, paymentMode, uri, openingStock, stockSold, stockRemaining, customFieldsJson, paymentDetails, enteredBy, productId ->
                    viewModel.addTransaction(amount, dialogType, timestamp, desc, category, paymentMode, uri, openingStock, stockSold, stockRemaining, customFieldsJson, paymentDetails, enteredBy, productId)
                    showAddDialog = false
                },
                onUpdate = { updatedTransaction ->
                    viewModel.updateTransaction(updatedTransaction)
                    showAddDialog = false
                },
                onDelete = { transaction ->
                    viewModel.deleteTransaction(transaction)
                    showAddDialog = false
                },
                onSoftDelete = { transaction ->
                    viewModel.softDeleteTransaction(transaction)
                    showAddDialog = false
                },
                onAddCategory = { newCategory ->
                    viewModel.addCategory(newCategory)
                },
                onAddCustomField = { name, type ->
                    viewModel.addCustomField(name, type)
                }
            )
        }
    }
}

@Composable
fun CombinedBalanceCard(
    modifier: Modifier = Modifier, 
    totalBalance: Double, 
    todayBalance: Double,
    totalCashBalance: Double,
    totalOnlineBalance: Double,
    todayCashBalance: Double,
    todayOnlineBalance: Double,
    onReportClick: () -> Unit = {},
    onDeletedTransactionsClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("₹ ${"%.2f".format(totalBalance)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (totalBalance >= 0) Color.Black else Color.Red)
                    Text("Total Balance", fontSize = 11.sp, color = Color.Gray)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cash in Hand", fontSize = 10.sp, color = Color.Gray)
                        Text("₹ ${"%.2f".format(totalCashBalance)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Online", fontSize = 10.sp, color = Color.Gray)
                        Text("₹ ${"%.2f".format(totalOnlineBalance)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                VerticalDivider(
                    modifier = Modifier
                        .height(70.dp)
                        .padding(horizontal = 12.dp),
                    color = Color.LightGray
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("₹ ${"%.2f".format(todayBalance)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (todayBalance >= 0) Color.Black else Color.Red)
                    Text("Today's Balance", fontSize = 11.sp, color = Color.Gray)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cash in Hand", fontSize = 10.sp, color = Color.Gray)
                        Text("₹ ${"%.2f".format(todayCashBalance)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Online", fontSize = 10.sp, color = Color.Gray)
                        Text("₹ ${"%.2f".format(todayOnlineBalance)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            HorizontalDivider(color = Color.LightGray)
            
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onReportClick,
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0052CC))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("VIEW CASHBOOK REPORT", color = Color(0xFF0052CC), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                VerticalDivider(modifier = Modifier.height(36.dp), color = Color.LightGray)
                TextButton(
                    onClick = onDeletedTransactionsClick,
                    modifier = Modifier.weight(1f).height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0052CC))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DELETED TRANSACTIONS", color = Color(0xFF0052CC), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomActionButtons(onOutClick: () -> Unit, onInClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onOutClick,
            modifier = Modifier.weight(1f).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("OUT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = onInClick,
            modifier = Modifier.weight(1f).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("IN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.clipboard_illustration),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Hello! Let's make today's entries", color = Color.Gray, fontSize = 16.sp)
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE3F2FD))
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add Your First Entry", color = Color(0xFF0052CC), fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedDownArrow()
        }
    }
}

@Composable
fun AnimatedDownArrow() {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    Icon(
        Icons.Default.ArrowDownward,
        contentDescription = "Down",
        tint = Color(0xFF0052CC),
        modifier = Modifier.offset(y = offsetY.dp)
    )
}

@Composable
fun SummaryRow(dateString: String, inAmount: Double, outAmount: Double, numEntries: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(dateString, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("$numEntries Entry", color = Color.Gray, fontSize = 14.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(horizontalAlignment = Alignment.End) {
                Text("OUT", color = Color.Gray, fontSize = 12.sp)
                Text("₹ ${"%.0f".format(outAmount)}", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("IN", color = Color.Gray, fontSize = 12.sp)
                Text("₹ ${"%.0f".format(inAmount)}", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: TransactionEntity, 
    isDeletedView: Boolean = false,
    showCheckbox: Boolean = false,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: (TransactionEntity) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateString = dateFormat.format(Date(transaction.timestamp))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { onLongClick(transaction) }
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showCheckbox) {
                androidx.compose.material3.Checkbox(
                    checked = isChecked,
                    onCheckedChange = { onCheckedChange(it) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(transaction.category, fontWeight = FontWeight.Bold, color = Color(0xFF0052CC), fontSize = 12.sp)
                    if (transaction.receiptUri != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Image, contentDescription = "Receipt attached", modifier = Modifier.size(14.dp), tint = Color.Gray)
                    }
                }
                Text(transaction.description?.takeIf { it.isNotBlank() } ?: "Entry", fontWeight = FontWeight.Bold)
                
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(dateString, fontSize = 12.sp, color = Color.Gray)
                    Text(" • ", fontSize = 12.sp, color = Color.Gray)
                    
                    val paymentIcon = when(transaction.paymentMode) {
                        "Cash" -> Icons.Default.Money
                        "UPI" -> Icons.Default.Smartphone
                        "Bank Transfer" -> Icons.Default.AccountBalance
                        "Debit Card" -> Icons.Default.CreditCard
                        "Credit Card" -> Icons.Default.Payment
                        else -> Icons.Default.Money
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(paymentIcon, contentDescription = transaction.paymentMode, modifier = Modifier.size(12.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(2.dp))
                        val paymentText = if (!transaction.paymentDetails.isNullOrBlank()) {
                            "${transaction.paymentMode} (${transaction.paymentDetails})"
                        } else {
                            transaction.paymentMode
                        }
                        Text(paymentText, fontSize = 12.sp, color = Color.Gray)
                    }

                    if (transaction.stockSold != null) {
                        Text(" • ", fontSize = 12.sp, color = Color.Gray)
                        Text("Sold: ${transaction.stockSold}", fontSize = 12.sp, color = Color.Gray)
                    }
                    if (transaction.stockRemaining != null) {
                        Text(" • ", fontSize = 12.sp, color = Color.Gray)
                        Text("Rem: ${transaction.stockRemaining}", fontSize = 12.sp, color = Color.Gray)
                    }

                    if (!transaction.enteredBy.isNullOrBlank()) {
                        Text(" • ", fontSize = 12.sp, color = Color.Gray)
                        Text("By: ${transaction.enteredBy}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                
                if (transaction.customFields != null) {
                    val parsedFields = remember(transaction.customFields) {
                        val fields = mutableListOf<Pair<String, String>>()
                        try {
                            val json = org.json.JSONObject(transaction.customFields)
                            val keys = json.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                val value = json.getString(key)
                                if (value.isNotBlank()) {
                                    fields.add(key to value)
                                }
                            }
                        } catch (e: Exception) {
                            // ignore malformed json
                        }
                        fields
                    }
                    
                    if (parsedFields.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        parsedFields.forEach { (key, value) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$key: ", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                Text(value, fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                
                if (isDeletedView && transaction.deletedAt != null) {
                    val deletedDateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(transaction.deletedAt))
                    
                    // Calculate based on calendar days, not 24-hour exact periods
                    val currentCal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                    val deletedCal = Calendar.getInstance().apply {
                        timeInMillis = transaction.deletedAt
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                    
                    val daysPassed = ((currentCal.timeInMillis - deletedCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                    val daysRemaining = java.lang.Math.max(0, 7 - daysPassed)
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Deleted on: $deletedDateStr", fontSize = 11.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                    Text("Active for: $daysRemaining days", fontSize = 11.sp, color = Color(0xFFD32F2F))
                }
            }
            Text(
                text = "₹ ${"%.2f".format(transaction.amount)}",
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.IN) Color(0xFF388E3C) else Color(0xFFD32F2F)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    type: TransactionType,
    editingTransaction: TransactionEntity? = null,
    categories: List<CategoryEntity>,
    customFieldsList: List<com.example.paddupushtakam.data.CustomFieldEntity>,
    products: List<com.example.paddupushtakam.data.ProductEntity>,
    onDismiss: () -> Unit,
    onAdd: (Double, String, String, Long, String, String?, Int?, Int?, Int?, String?, String?, String?, Int?) -> Unit,
    onUpdate: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit,
    onSoftDelete: (TransactionEntity) -> Unit,
    onAddCategory: (String) -> Unit,
    onAddCustomField: (String, String) -> Unit
) {
    var amount by remember { mutableStateOf(TextFieldValue(editingTransaction?.amount?.let { "%.2f".format(it) } ?: "0.00", TextRange((editingTransaction?.amount?.let { "%.2f".format(it) } ?: "0.00").length))) }
    var description by remember { mutableStateOf(editingTransaction?.description ?: "") }
    var selectedCategory by remember { mutableStateOf<String?>(editingTransaction?.category) }
    var receiptUri by remember { mutableStateOf<Uri?>(editingTransaction?.receiptUri?.let { Uri.parse(it) }) }
    var expanded by remember { mutableStateOf(false) }
    var paymentModeExpanded by remember { mutableStateOf(false) }
    var selectedPaymentMode by remember { mutableStateOf(editingTransaction?.paymentMode ?: "Cash") }
    var paymentDetails by remember { mutableStateOf(editingTransaction?.paymentDetails ?: "") }
    var enteredBy by remember { mutableStateOf(editingTransaction?.enteredBy ?: "") }
    var openingStock by remember { mutableStateOf(editingTransaction?.openingStock?.toString() ?: "") }
    var stockSold by remember { mutableStateOf(editingTransaction?.stockSold?.toString() ?: "") }
    var stockRemaining by remember { mutableStateOf(editingTransaction?.stockRemaining?.toString() ?: "") }
    var stockDropdownExpanded by remember { mutableStateOf(false) }
    var selectedStockType by remember { mutableStateOf("Opening Stock") }
    var selectedProduct by remember { mutableStateOf<com.example.paddupushtakam.data.ProductEntity?>(
        editingTransaction?.productId?.let { id -> products.find { it.id == id } }
    ) }
    var productDropdownExpanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var showCalculator by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(0) }
    val totalSteps = if (customFieldsList.isNotEmpty()) 3 else 2
    var showCustomFieldBuilder by remember { mutableStateOf(false) }
    var showCustomFieldPrompt by remember { mutableStateOf(false) }
    var customFieldValues by remember { 
        mutableStateOf(
            try {
                if (editingTransaction?.customFields != null) {
                    val json = org.json.JSONObject(editingTransaction.customFields)
                    val map = mutableMapOf<String, String>()
                    json.keys().forEach { map[it] = json.getString(it) }
                    map
                } else emptyMap<String, String>()
            } catch (e: Exception) { emptyMap<String, String>() }
        )
    }
    var hasPromptedForCustomFields by remember { mutableStateOf(false) }
    var pendingSaveAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val calculatorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val paymentModes = listOf("Cash", "UPI", "Bank Transfer", "Debit Card", "Credit Card")
    
    // Date Picker state
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = editingTransaction?.timestamp ?: System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> receiptUri = uri }
    )

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val matchedProduct = products.find { it.barcode == result.contents }
            if (matchedProduct != null) {
                selectedProduct = matchedProduct
                openingStock = matchedProduct.currentStock.toString()
                if (stockSold.isNotBlank()) {
                    val sold = stockSold.toIntOrNull()
                    if (sold != null) {
                        stockRemaining = if (type == TransactionType.IN) {
                            (matchedProduct.currentStock - sold).toString()
                        } else {
                            (matchedProduct.currentStock + sold).toString()
                        }
                    }
                } else {
                    stockRemaining = ""
                }
            } else {
                Toast.makeText(context, "No product found for barcode: ${result.contents}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showNewCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        onAddCategory(newCategoryName)
                        selectedCategory = newCategoryName
                    }
                    showNewCategoryDialog = false
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showCustomFieldPrompt) {
        AlertDialog(
            onDismissRequest = { 
                showCustomFieldPrompt = false
                pendingSaveAction?.invoke()
                pendingSaveAction = null
            },
            title = { Text("Add Custom UI Controls?") },
            text = { Text("Do you want to add a new form with new UI Controls?") },
            confirmButton = {
                Button(onClick = {
                    showCustomFieldPrompt = false
                    showCustomFieldBuilder = true
                }) { Text("Yes") }
            },
            dismissButton = {
                Button(onClick = {
                    showCustomFieldPrompt = false
                    pendingSaveAction?.invoke()
                    pendingSaveAction = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("No") }
            }
        )
    }

    if (showCustomFieldBuilder) {
        var newFieldName by remember { mutableStateOf("") }
        var newFieldType by remember { mutableStateOf("Text") }
        var expandedType by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showCustomFieldBuilder = false },
            title = { Text("Custom Form Builder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newFieldName,
                        onValueChange = { newFieldName = it },
                        label = { Text("Field Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType }
                    ) {
                        OutlinedTextField(
                            value = newFieldType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Field Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            listOf("Text", "Number").forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        newFieldType = type
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newFieldName.isNotBlank()) {
                        onAddCustomField(newFieldName, newFieldType)
                        showCustomFieldBuilder = false
                    }
                }) { Text("Add Field") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomFieldBuilder = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteConfirmDialog && editingTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Transaction") },
            text = { Text("Deleting wouldnt delete permanently but would be stored for a week. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = { 
                        onSoftDelete(editingTransaction)
                        showDeleteConfirmDialog = false
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        // Just close the dialog, do not delete
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) { Text("No") }
            }
        )
    }

    // Colors matching the mockup/theme
    val mainColor = if (type == TransactionType.IN) Color(0xFF0066FF) else Color(0xFFD32F2F)
    val gradientColors = if (type == TransactionType.IN) {
        listOf(Color(0xFF293291), Color(0xFF0099FF)) // Deep blue to light blue like mockup
    } else {
        listOf(Color(0xFFB71C1C), Color(0xFFEF5350))
    }

    if (showCalculator) {
        ModalBottomSheet(
            onDismissRequest = { 
                showCalculator = false 
                focusManager.clearFocus()
            },
            sheetState = calculatorSheetState,
            containerColor = Color.White
        ) {
            CalculatorComponent(
                onKeyClick = { key ->
                    if (key == "=") {
                        val result = evaluateExpression(amount.text)
                        if (result != "Error") {
                            amount = TextFieldValue(result, TextRange(result.length))
                        }
                    } else {
                        // Clear "0.00" initial value if starting to type a number
                        val currentText = if (amount.text == "0.00" && (key.first().isDigit() || key == ".")) "" else amount.text
                        val newText = currentText + key
                        amount = TextFieldValue(newText, TextRange(newText.length))
                    }
                },
                onBackspace = {
                    if (amount.text.isNotEmpty()) {
                        val newText = amount.text.dropLast(1)
                        amount = TextFieldValue(newText, TextRange(newText.length))
                    }
                },
                onDone = {
                    val result = evaluateExpression(amount.text)
                    if (result != "Error") {
                        amount = TextFieldValue(result, TextRange(result.length))
                    }
                    showCalculator = false
                    focusManager.clearFocus()
                },
                mainColor = mainColor
            )
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = androidx.compose.ui.graphics.RectangleShape,
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header with Back, Title, Close
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.horizontalGradient(colors = gradientColors))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text(
                            text = if (editingTransaction != null) "Edit Transaction" else if (type == TransactionType.IN) "Cash In" else "Cash Out",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (editingTransaction != null) {
                                IconButton(onClick = { showDeleteConfirmDialog = true }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                }
                            }
                            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }
                }
                

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Step indicator
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in 0 until totalSteps) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .height(8.dp)
                                    .weight(1f)
                                    .background(
                                        if (i <= currentStep) mainColor else Color.LightGray,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                slideInHorizontally(animationSpec = tween(300)) { width -> width } togetherWith
                                        slideOutHorizontally(animationSpec = tween(300)) { width -> -width }
                            } else {
                                slideInHorizontally(animationSpec = tween(300)) { width -> -width } togetherWith
                                        slideOutHorizontally(animationSpec = tween(300)) { width -> width }
                            }
                        },
                        label = "form_step_animation"
                    ) { targetStep ->
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            when (targetStep) {
                                0 -> {
                                    // Step 1: Main Transaction Details
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Amount (₹)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        OutlinedTextField(
                                            value = amount,
                                            onValueChange = { amount = it },
                                            prefix = { Text("₹ ") },
                                            readOnly = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .onFocusChanged { if (it.isFocused) showCalculator = true },
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = mainColor,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Notes", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        OutlinedTextField(
                                            value = description,
                                            onValueChange = { description = it },
                                            placeholder = { Text("Add details...") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, autoCorrect = true),
                                            minLines = 1,
                                            maxLines = 5,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = mainColor,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        ExposedDropdownMenuBox(
                                            expanded = expanded && description.isNotBlank(),
                                            onExpandedChange = { if (description.isNotBlank()) expanded = !expanded }
                                        ) {
                                            OutlinedTextField(
                                                value = selectedCategory ?: "",
                                                onValueChange = {},
                                                readOnly = true,
                                                enabled = description.isNotBlank(),
                                                placeholder = { Text("Select Category") },
                                                leadingIcon = { Icon(Icons.Default.List, contentDescription = null) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = mainColor,
                                                    focusedContainerColor = Color(0xFFF5F7FA),
                                                    unfocusedContainerColor = Color(0xFFF5F7FA),
                                                    unfocusedBorderColor = Color.Transparent
                                                )
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                categories.forEach { category ->
                                                    DropdownMenuItem(
                                                        text = { Text(category.name) },
                                                        onClick = {
                                                            selectedCategory = category.name
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                                DropdownMenuItem(
                                                    text = { Text("+ Add New Category", color = mainColor, fontWeight = FontWeight.Bold) },
                                                    onClick = {
                                                        expanded = false
                                                        showNewCategoryDialog = true
                                                    }
                                                )
                                            }
                                        }
                                        
                                        if (categories.isNotEmpty()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                categories.take(3).forEach { category ->
                                                    AssistChip(
                                                        onClick = { if (description.isNotBlank()) selectedCategory = category.name },
                                                        enabled = description.isNotBlank(),
                                                        label = { Text(category.name) },
                                                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                        colors = AssistChipDefaults.assistChipColors(
                                                            containerColor = if (selectedCategory == category.name) mainColor.copy(alpha = 0.1f) else Color.Transparent
                                                        ),
                                                        border = BorderStroke(
                                                            1.dp,
                                                            if (selectedCategory == category.name) mainColor else Color.LightGray
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Date", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        val selectedDateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                                        val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
                                        
                                        OutlinedTextField(
                                            value = formattedDate,
                                            onValueChange = {},
                                            readOnly = true,
                                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { showDatePicker = true },
                                            enabled = false,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = Color.Black,
                                                disabledBorderColor = Color.LightGray,
                                                disabledLeadingIconColor = Color.Gray,
                                                disabledContainerColor = Color.White
                                            )
                                        )
                                    }
                                }
                                1 -> {
                                    // Step 2: Payment & Attachments
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                                            Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            ExposedDropdownMenuBox(
                                                expanded = paymentModeExpanded,
                                                onExpandedChange = { paymentModeExpanded = !paymentModeExpanded }
                                            ) {
                                                OutlinedTextField(
                                                    value = selectedPaymentMode,
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null) },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentModeExpanded) },
                                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = mainColor,
                                                        focusedContainerColor = Color(0xFFF5F7FA),
                                                        unfocusedContainerColor = Color(0xFFF5F7FA),
                                                        unfocusedBorderColor = Color.Transparent
                                                    )
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = paymentModeExpanded,
                                                    onDismissRequest = { paymentModeExpanded = false }
                                                ) {
                                                    paymentModes.forEach { mode ->
                                                        DropdownMenuItem(
                                                            text = { Text(mode) },
                                                            onClick = {
                                                                selectedPaymentMode = mode
                                                                paymentModeExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (products.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Select Product (Optional)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                IconButton(
                                                    onClick = {
                                                        val options = ScanOptions()
                                                        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
                                                        options.setPrompt("Scan a Product Barcode")
                                                        options.setBeepEnabled(true)
                                                        options.setBarcodeImageEnabled(true)
                                                        barcodeLauncher.launch(options)
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan Barcode", tint = mainColor)
                                                }
                                            }
                                            ExposedDropdownMenuBox(
                                                expanded = productDropdownExpanded,
                                                onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                                            ) {
                                                OutlinedTextField(
                                                    value = selectedProduct?.name ?: "None",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = mainColor,
                                                        focusedContainerColor = Color.White,
                                                        unfocusedContainerColor = Color.White
                                                    )
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = productDropdownExpanded,
                                                    onDismissRequest = { productDropdownExpanded = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("None") },
                                                        onClick = {
                                                            selectedProduct = null
                                                            productDropdownExpanded = false
                                                        }
                                                    )
                                                    products.forEach { product ->
                                                        DropdownMenuItem(
                                                            text = { Text(product.name) },
                                                            onClick = {
                                                                selectedProduct = product
                                                                openingStock = product.currentStock.toString()
                                                                if (stockSold.isNotBlank()) {
                                                                    val sold = stockSold.toIntOrNull()
                                                                    if (sold != null) {
                                                                        stockRemaining = if (type == com.example.paddupushtakam.data.TransactionType.IN) {
                                                                            (product.currentStock - sold).toString()
                                                                        } else {
                                                                            (product.currentStock + sold).toString()
                                                                        }
                                                                    }
                                                                } else {
                                                                    stockRemaining = ""
                                                                }
                                                                productDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (selectedProduct != null) {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            OutlinedTextField(
                                                value = stockSold,
                                                onValueChange = { newValue ->
                                                    stockSold = newValue
                                                    openingStock = selectedProduct!!.currentStock.toString()
                                                    val sold = newValue.toIntOrNull()
                                                    if (sold != null) {
                                                        stockRemaining = if (type == com.example.paddupushtakam.data.TransactionType.IN) {
                                                            (selectedProduct!!.currentStock - sold).toString()
                                                        } else {
                                                            (selectedProduct!!.currentStock + sold).toString()
                                                        }
                                                    } else {
                                                        stockRemaining = ""
                                                    }
                                                },
                                                label = { Text("Quantity") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = mainColor,
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                )
                                            )
                                            val newStockText = stockRemaining.takeIf { it.isNotBlank() } ?: selectedProduct!!.currentStock.toString()
                                            Text(
                                                text = "Current Stock: ${selectedProduct!!.currentStock} -> New Stock: $newStockText",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                            )
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text("Stock (Manual Tracking)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                ExposedDropdownMenuBox(
                                                    expanded = stockDropdownExpanded,
                                                    onExpandedChange = { stockDropdownExpanded = !stockDropdownExpanded },
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    OutlinedTextField(
                                                        value = selectedStockType,
                                                        onValueChange = {},
                                                        readOnly = true,
                                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stockDropdownExpanded) },
                                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = mainColor,
                                                            focusedContainerColor = Color(0xFFF5F7FA),
                                                            unfocusedContainerColor = Color(0xFFF5F7FA),
                                                            unfocusedBorderColor = Color.Transparent
                                                        )
                                                    )
                                                    ExposedDropdownMenu(
                                                        expanded = stockDropdownExpanded,
                                                        onDismissRequest = { stockDropdownExpanded = false }
                                                    ) {
                                                        listOf("Opening Stock", "Stock Sold", "Stock Remaining").forEach { stockType ->
                                                            DropdownMenuItem(
                                                                text = { Text(stockType) },
                                                                onClick = {
                                                                    selectedStockType = stockType
                                                                    stockDropdownExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                                
                                                OutlinedTextField(
                                                    value = when(selectedStockType) {
                                                        "Opening Stock" -> openingStock
                                                        "Stock Sold" -> stockSold
                                                        else -> stockRemaining
                                                    },
                                                    onValueChange = { newValue -> 
                                                        when(selectedStockType) {
                                                            "Opening Stock" -> {
                                                                openingStock = newValue
                                                                if (openingStock.isNotBlank() && stockSold.isNotBlank()) {
                                                                    val open = openingStock.toIntOrNull()
                                                                    val sold = stockSold.toIntOrNull()
                                                                    if (open != null && sold != null) {
                                                                        stockRemaining = (open - sold).toString()
                                                                    }
                                                                } else {
                                                                    stockRemaining = ""
                                                                }
                                                            }
                                                            "Stock Sold" -> {
                                                                stockSold = newValue
                                                                if (openingStock.isNotBlank() && stockSold.isNotBlank()) {
                                                                    val open = openingStock.toIntOrNull()
                                                                    val sold = stockSold.toIntOrNull()
                                                                    if (open != null && sold != null) {
                                                                        stockRemaining = (open - sold).toString()
                                                                    }
                                                                } else {
                                                                    stockRemaining = ""
                                                                }
                                                            }
                                                            "Stock Remaining" -> {
                                                                stockRemaining = newValue
                                                            }
                                                        }
                                                    },
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                    singleLine = true,
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = mainColor,
                                                        focusedContainerColor = Color.White,
                                                        unfocusedContainerColor = Color.White
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    
                                    if (selectedPaymentMode != "Cash") {
                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text("Payment Details :", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            OutlinedTextField(
                                                value = paymentDetails,
                                                onValueChange = { paymentDetails = it },
                                                placeholder = { Text("Enter details (e.g., UTR, Card No)") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = mainColor,
                                                    focusedContainerColor = Color.White,
                                                    unfocusedContainerColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Entered By :", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        OutlinedTextField(
                                            value = enteredBy,
                                            onValueChange = { enteredBy = it },
                                            placeholder = { Text("Your Name") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = mainColor,
                                                focusedContainerColor = Color.White,
                                                unfocusedContainerColor = Color.White
                                            )
                                        )
                                    }

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Attach Receipt", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Button(
                                                onClick = { photoPickerLauncher.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                                modifier = Modifier.weight(1f).height(48.dp),
                                                shape = RoundedCornerShape(24.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Brush.horizontalGradient(colors = gradientColors)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(if (receiptUri == null) "Add Receipt" else "Change Receipt", color = Color.White, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                            
                                            if (receiptUri != null) {
                                                AsyncImage(
                                                    model = receiptUri,
                                                    contentDescription = "Receipt Thumbnail",
                                                    modifier = Modifier.size(48.dp),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                                2 -> {
                                    // Step 3: Custom Fields
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (customFieldsList.isEmpty()) {
                                            Text("No custom fields defined.", color = Color.Gray, fontSize = 12.sp)
                                        }
                                        customFieldsList.forEach { field ->
                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text(field.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                OutlinedTextField(
                                                    value = customFieldValues[field.name] ?: "",
                                                    onValueChange = { customFieldValues = customFieldValues.toMutableMap().apply { put(field.name, it) } },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = mainColor,
                                                        focusedContainerColor = Color.White,
                                                        unfocusedContainerColor = Color.White
                                                    ),
                                                    keyboardOptions = if (field.type == "Number") KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        // OutlinedButton(onClick = { showCustomFieldBuilder = true }, modifier = Modifier.fillMaxWidth()) {
                                        //     Text("+ Add Another Custom Field")
                                        // }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Bottom Navigation Bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(
                            onClick = { currentStep -= 1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    
                    if (currentStep < totalSteps - 1) {
                        Button(
                            onClick = {
                                if (currentStep == 0 && (amount.text.isBlank() || description.isBlank() || amount.text.toDoubleOrNull() == 0.0)) {
                                    android.widget.Toast.makeText(context, "Please fill Amount and Notes properly", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    currentStep += 1
                                }
                            },
                            modifier = if (currentStep == 0) Modifier.fillMaxWidth() else Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = mainColor)
                        ) {
                            androidx.compose.material3.Text("Next")
                        }
                    } else {
                        Button(
                            onClick = {
                                val performSave = {
                                    val amountDouble = amount.text.toDoubleOrNull() ?: 0.0
                                    val nowMillis = System.currentTimeMillis()
                                    val selectedTimestamp = datePickerState.selectedDateMillis?.let { selected ->
                                        val calSelected = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = selected }
                                        val calNow = java.util.Calendar.getInstance()
                                        if (calSelected.get(java.util.Calendar.YEAR) == calNow.get(java.util.Calendar.YEAR) &&
                                            calSelected.get(java.util.Calendar.DAY_OF_YEAR) == calNow.get(java.util.Calendar.DAY_OF_YEAR)) {
                                            nowMillis
                                        } else {
                                            selected
                                        }
                                    } ?: nowMillis
                                    val finalCategory = selectedCategory ?: categories.firstOrNull()?.name ?: "Sales"
                                    
                                    if (amountDouble > 0 && description.isNotBlank() && enteredBy.isNotBlank()) {
                                        val descLower = description.lowercase()
                                        val catLower = finalCategory.lowercase()
                                        
                                        if (type == com.example.paddupushtakam.data.TransactionType.IN && (descLower.contains("purchase") || catLower.contains("purchase"))) {
                                            android.widget.Toast.makeText(context, "Purchases indicate money going out. Please use Cash Out instead.", android.widget.Toast.LENGTH_LONG).show()
                                        } else if (type == com.example.paddupushtakam.data.TransactionType.OUT && (descLower.contains("sale") || catLower.contains("sale"))) {
                                            android.widget.Toast.makeText(context, "Sales indicate money coming in. Please use Cash In instead.", android.widget.Toast.LENGTH_LONG).show()
                                        } else {
                                            val customFieldsJson = if (customFieldValues.isNotEmpty()) org.json.JSONObject(customFieldValues).toString() else null
                                            if (editingTransaction != null) {
                                                onUpdate(
                                                    editingTransaction.copy(
                                                        amount = amountDouble,
                                                        description = description,
                                                        category = finalCategory,
                                                        timestamp = selectedTimestamp,
                                                        paymentMode = selectedPaymentMode,
                                                        receiptUri = receiptUri?.toString(),
                                                        openingStock = openingStock.toIntOrNull(),
                                                        stockSold = stockSold.toIntOrNull(),
                                                        stockRemaining = stockRemaining.toIntOrNull(),
                                                        customFields = customFieldsJson,
                                                        paymentDetails = paymentDetails.takeIf { selectedPaymentMode != "Cash" && it.isNotBlank() },
                                                        enteredBy = enteredBy.takeIf { it.isNotBlank() },
                                                        productId = selectedProduct?.id
                                                    )
                                                )
                                            } else {
                                                onAdd(
                                                    amountDouble, 
                                                    description, 
                                                    finalCategory, 
                                                    selectedTimestamp, 
                                                    selectedPaymentMode, 
                                                    receiptUri?.toString(), 
                                                    openingStock.toIntOrNull(),
                                                    stockSold.toIntOrNull(),
                                                    stockRemaining.toIntOrNull(),
                                                    customFieldsJson,
                                                    paymentDetails.takeIf { selectedPaymentMode != "Cash" && it.isNotBlank() },
                                                    enteredBy.takeIf { it.isNotBlank() },
                                                    selectedProduct?.id
                                                )
                                            }
                                        }
                                    } else {
                                        android.widget.Toast.makeText(context, "Please fill Amount, Notes, and Entered By", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                                /*
                                if (!hasPromptedForCustomFields && customFieldsList.isEmpty()) {
                                    pendingSaveAction = performSave
                                    showCustomFieldPrompt = true
                                    hasPromptedForCustomFields = true
                                } else {
                                    performSave()
                                }
                                */
                                performSave()
                            },
                            enabled = amount.text.isNotBlank() && description.isNotBlank() && enteredBy.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = mainColor)
                        ) {
                            androidx.compose.material3.Text(if (editingTransaction != null) "Update" else "Save", fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
