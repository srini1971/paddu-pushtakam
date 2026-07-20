package com.example.paddupushtakam.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: TransactionType,
    val timestamp: Long,
    val description: String? = null,
    val category: String = "Sales",
    val paymentMode: String = "Cash",
    val receiptUri: String? = null,
    val deletedAt: Long? = null,
    val openingStock: Int? = null,
    val stockSold: Int? = null,
    val stockRemaining: Int? = null,
    val customFields: String? = null,
    val paymentDetails: String? = null,
    val enteredBy: String? = null,
    val productId: Int? = null
)

enum class TransactionType {
    IN, OUT
}
