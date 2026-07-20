package com.example.paddupushtakam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import androidx.compose.runtime.rememberCoroutineScope
import com.example.paddupushtakam.data.AppDatabase
import java.io.FileInputStream
import java.io.FileOutputStream

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDownload
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.paddupushtakam.data.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashbookReportScreen(
    viewModel: TransactionViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var durationDropdownExpanded by remember { mutableStateOf(false) }
    var selectedDuration by remember { mutableStateOf("DATE RANGE") }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        uri?.let { destUri ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(context)
                    db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
                    
                    val dbFile = context.getDatabasePath("paddu_pushtakam_database")
                    if (dbFile.exists()) {
                        context.contentResolver.openOutputStream(destUri)?.use { outputStream ->
                            FileInputStream(dbFile).use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Backup successful!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { sourceUri ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(context)
                    db.close()
                    
                    val dbFile = context.getDatabasePath("paddu_pushtakam_database")
                    context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                        FileOutputStream(dbFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Restore successful! Please completely close and restart the app.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    launch(Dispatchers.Main) {
                        Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    val filteredTransactions = remember(allTransactions, selectedDuration, startDate, endDate) {
        val calendar = Calendar.getInstance()
        
        when (selectedDuration) {
            "All" -> allTransactions
            "This Month" -> {
                calendar.timeInMillis = System.currentTimeMillis()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)
                
                allTransactions.filter {
                    calendar.timeInMillis = it.timestamp
                    calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear
                }
            }
            "Last Month" -> {
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.MONTH, -1)
                val lastMonth = calendar.get(Calendar.MONTH)
                val yearOfLastMonth = calendar.get(Calendar.YEAR)
                
                allTransactions.filter {
                    calendar.timeInMillis = it.timestamp
                    calendar.get(Calendar.MONTH) == lastMonth && calendar.get(Calendar.YEAR) == yearOfLastMonth
                }
            }
            "Last Week" -> {
                val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
                allTransactions.filter { it.timestamp >= sevenDaysAgo }
            }
            "Single Day" -> {
                calendar.timeInMillis = startDate
                val day = calendar.get(Calendar.DAY_OF_YEAR)
                val year = calendar.get(Calendar.YEAR)
                
                allTransactions.filter {
                    calendar.timeInMillis = it.timestamp
                    calendar.get(Calendar.DAY_OF_YEAR) == day && calendar.get(Calendar.YEAR) == year
                }
            }
            "DATE RANGE" -> {
                calendar.timeInMillis = startDate
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                
                calendar.timeInMillis = endDate
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                
                allTransactions.filter { it.timestamp in start..end }
            }
            else -> allTransactions
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= endDate
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis >= startDate
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = it }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cashbook Report", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0052CC))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF0F2F5))
        ) {
            // Date Range Selector Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start Date Control (Left)
                    Row(
                        modifier = Modifier.clickable { showStartDatePicker = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Start Date", tint = Color(0xFF0052CC))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateFormat.format(Date(startDate)), color = Color.DarkGray)
                    }

                    Text("to", color = Color.Gray)

                    // End Date Control (Right)
                    Row(
                        modifier = Modifier.clickable { showEndDatePicker = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = dateFormat.format(Date(endDate)), color = Color.DarkGray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.DateRange, contentDescription = "End Date", tint = Color(0xFF0052CC))
                    }
                }
            }

            // Duration Selector Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Part
                    Box(modifier = Modifier.weight(1f)) {
                        Text("Select Report Duration", color = Color.DarkGray)
                    }
                    
                    // Right Part (Duration Selector)
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                        Row(
                            modifier = Modifier.clickable { durationDropdownExpanded = true },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedDuration, color = Color.DarkGray)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.Gray)
                        }
                    }
                }
            }

            if (durationDropdownExpanded) {
                AlertDialog(
                    onDismissRequest = { durationDropdownExpanded = false },
                    title = { Text("Select Report Duration") },
                    text = {
                        Column {
                            val options = listOf("DATE RANGE", "This Month", "Single Day", "Last Week", "Last Month", "All")
                            options.forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            selectedDuration = option
                                            durationDropdownExpanded = false
                                        }
                                        .padding(vertical = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedDuration == option,
                                        onClick = {
                                            selectedDuration = option
                                            durationDropdownExpanded = false
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = option)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { durationDropdownExpanded = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            // 3rd Edit Control: Transactions List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (filteredTransactions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No transactions found for this period", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredTransactions) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onLongClick = { /* Can implement edit from report later if needed */ }
                            )
                        }
                    }
                }
            }

            // 4th Control: Export Buttons
            val context = LocalContext.current
            
            fun shareFile(file: java.io.File, mimeType: String) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TITLE, "Paddu Pushtakam Report")
                    
                    clipData = android.content.ClipData.newUri(context.contentResolver, "Paddu Pushtakam Report", uri)
                    
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooserIntent = Intent.createChooser(intent, "Share Report via")
                chooserIntent.clipData = intent.clipData
                chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(chooserIntent)
            }

            // 3.5 Trust Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Safe", tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% Local & Safe: These reports are generated directly on your phone and contain no scripts or malware.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = Color(0xFF1B5E20)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { 
                        val file = ReportGenerator.generatePDF(context, filteredTransactions)
                        shareFile(file, "application/pdf")
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = { 
                        val file = ReportGenerator.generateCSV(context, filteredTransactions)
                        shareFile(file, "text/csv")
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("EXCEL", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = { 
                        val file = ReportGenerator.generateCSV(context, filteredTransactions)
                        shareFile(file, "text/csv")
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                    TextButton(onClick = {
                        val file = ReportGenerator.generatePDF(context, filteredTransactions) // Default share is PDF
                        shareFile(file, "application/pdf")
                    }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
            
            // 4. Data Management (Backup/Restore)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = "Backup", tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Data Management (Google Drive)",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            onClick = {
                                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val date = sdf.format(java.util.Date())
                                backupLauncher.launch("Paddu_Pushtakam_Backup_$date.db")
                            },
                            modifier = Modifier.weight(1f).background(Color(0xFFE65100), RoundedCornerShape(4.dp))
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "Backup", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BACKUP", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        TextButton(
                            onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier.weight(1f).background(Color(0xFF0052CC), RoundedCornerShape(4.dp))
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Restore", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RESTORE", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
