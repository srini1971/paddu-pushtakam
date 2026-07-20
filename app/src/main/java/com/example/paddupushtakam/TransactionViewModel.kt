package com.example.paddupushtakam

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.paddupushtakam.data.AppDatabase
import com.example.paddupushtakam.data.CategoryEntity
import com.example.paddupushtakam.data.ProductEntity
import com.example.paddupushtakam.data.TransactionEntity
import com.example.paddupushtakam.data.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import org.json.JSONObject

data class SmartScanData(
    val amount: String,
    val description: String,
    val category: String,
    val dateMillis: Long
)

/**
 * TransactionViewModel acts as the bridge between the UI (Screens) and the Data Layer (Database).
 * It holds the state of the UI and survives configuration changes (like screen rotations).
 * It fetches data from the DAO and exposes it as 'StateFlow', which the UI continuously observes.
 */
class TransactionViewModel(application: Application) : AndroidViewModel(application) {
    // Get an instance of our DAO to interact with the database
    private val dao = AppDatabase.getDatabase(application).transactionDao()

    private val _smartScanResult = MutableStateFlow<SmartScanData?>(null)
    val smartScanResult: StateFlow<SmartScanData?> = _smartScanResult

    private val _smartScanLoading = MutableStateFlow(false)
    val smartScanLoading: StateFlow<Boolean> = _smartScanLoading

    fun clearSmartScanResult() {
        _smartScanResult.value = null
    }

    fun analyzeReceipt(bitmap: Bitmap, apiKey: String) {
        viewModelScope.launch {
            _smartScanLoading.value = true
            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )
                
                val inputContent = content {
                    image(bitmap)
                    text("Analyze this receipt and extract the following information. Return ONLY a valid JSON object with the following keys and string values: 'amount' (just the number), 'description' (name of store or items), 'category' (a short general category), 'timestamp' (the unix timestamp in milliseconds for the date, or just empty string if not found).")
                }
                
                val response = generativeModel.generateContent(inputContent)
                val text = response.text ?: ""
                
                // Clean up the response in case Gemini wraps it in markdown like ```json ... ```
                val jsonString = text.replace("```json", "").replace("```", "").trim()
                
                val json = JSONObject(jsonString)
                val amount = json.optString("amount", "")
                val description = json.optString("description", "")
                val category = json.optString("category", "")
                val timestampStr = json.optString("timestamp", "")
                val dateMillis = timestampStr.toLongOrNull() ?: System.currentTimeMillis()
                
                _smartScanResult.value = SmartScanData(amount, description, category, dateMillis)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _smartScanLoading.value = false
            }
        }
    }

    // A reactive stream of ALL non-deleted transactions. 
    // When the database changes, this flow automatically updates the UI.
    val allTransactions: StateFlow<List<TransactionEntity>> = dao.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // A reactive stream of soft-deleted transactions.
    val deletedTransactions: StateFlow<List<TransactionEntity>> = dao.getDeletedTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIn: StateFlow<Double> = dao.getTotalAmountByType(TransactionType.IN)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        
    val totalOut: StateFlow<Double> = dao.getTotalAmountByType(TransactionType.OUT)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
        
    private val startOfToday: Long
    private val endOfToday: Long
    
    init {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        startOfToday = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        endOfToday = calendar.timeInMillis
        
        // Initialize default category
        viewModelScope.launch {
            dao.insertCategory(CategoryEntity("Sales"))
            
            // Purge transactions soft-deleted > 7 days ago
            val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)
            dao.deleteOldTransactions(sevenDaysAgo)
        }
    }

    val todayIn: StateFlow<Double> = dao.getAmountByTypeForPeriod(TransactionType.IN, startOfToday, endOfToday)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayOut: StateFlow<Double> = dao.getAmountByTypeForPeriod(TransactionType.OUT, startOfToday, endOfToday)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val productDao = AppDatabase.getDatabase(application).productDao()

    val allProducts: StateFlow<List<ProductEntity>> = productDao.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class ProductRevenue(val productName: String, val revenue: Double)

    val topProductsByRevenue: StateFlow<List<ProductRevenue>> = combine(allTransactions, allProducts) { transactions, products ->
        val productMap = products.associateBy { it.id }
        transactions.filter { it.type == TransactionType.IN && it.productId != null }
            .groupBy { it.productId!! }
            .map { (productId, txList) ->
                val revenue = txList.sumOf { it.amount }
                val productName = productMap[productId]?.name ?: "Unknown"
                ProductRevenue(productName, revenue)
            }
            .sortedByDescending { it.revenue }
            .take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class DailyRevenue(val dateLabel: String, val revenueIn: Double, val revenueOut: Double, val timestamp: Long)

    val revenueTrend: StateFlow<List<DailyRevenue>> = allTransactions.map { transactions ->
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val recentTx = transactions.filter { it.timestamp >= thirtyDaysAgo }
        
        val grouped = recentTx.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
        }
        
        grouped.map { (label, txList) ->
            val revIn = txList.filter { it.type == TransactionType.IN }.sumOf { it.amount }
            val revOut = txList.filter { it.type == TransactionType.OUT }.sumOf { it.amount }
            DailyRevenue(label, revIn, revOut, txList.first().timestamp)
        }.sortedBy { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addProduct(name: String, currentStock: Int, lowStockThreshold: Int = 5, barcode: String? = null) {
        viewModelScope.launch {
            productDao.insertProduct(ProductEntity(name = name, currentStock = currentStock, lowStockThreshold = lowStockThreshold, barcode = barcode))
        }
    }

    fun addTransaction(amount: Double, type: TransactionType, timestamp: Long = System.currentTimeMillis(), description: String = "", category: String = "Sales", paymentMode: String = "Cash", receiptUri: String? = null, openingStock: Int? = null, stockSold: Int? = null, stockRemaining: Int? = null, customFields: String? = null, paymentDetails: String? = null, enteredBy: String? = null, productId: Int? = null) {
        viewModelScope.launch {
            dao.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    type = type,
                    timestamp = timestamp,
                    description = description,
                    category = category,
                    paymentMode = paymentMode,
                    receiptUri = receiptUri,
                    openingStock = openingStock,
                    stockSold = stockSold,
                    stockRemaining = stockRemaining,
                    customFields = customFields,
                    paymentDetails = paymentDetails,
                    enteredBy = enteredBy,
                    productId = productId
                )
            )
            
            // Adjust master stock if a product is linked
            if (productId != null) {
                val product = productDao.getProductById(productId)
                if (product != null) {
                    val quantityChange = stockSold ?: 0
                    val newStock = if (type == TransactionType.IN) {
                        product.currentStock - quantityChange
                    } else {
                        product.currentStock + quantityChange
                    }
                    productDao.updateProduct(product.copy(currentStock = newStock))
                }
            }
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.updateTransaction(transaction)
        }
    }
    
    fun softDeleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.updateTransaction(transaction.copy(deletedAt = System.currentTimeMillis()))
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            dao.deleteTransaction(transaction)
        }
    }

    fun addCategory(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                dao.insertCategory(CategoryEntity(name))
            }
        }
    }
    
    private val customFieldDao = AppDatabase.getDatabase(application).customFieldDao()

    val customFields: StateFlow<List<com.example.paddupushtakam.data.CustomFieldEntity>> = customFieldDao.getAllCustomFields()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomField(name: String, type: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                customFieldDao.insertCustomField(com.example.paddupushtakam.data.CustomFieldEntity(name = name, type = type))
            }
        }
    }

    fun restoreTransactions(transactions: List<TransactionEntity>) {
        viewModelScope.launch {
            transactions.forEach { transaction ->
                dao.updateTransaction(transaction.copy(deletedAt = null))
            }
        }
    }
}
