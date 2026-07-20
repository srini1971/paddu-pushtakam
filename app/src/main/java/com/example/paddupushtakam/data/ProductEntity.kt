package com.example.paddupushtakam.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val currentStock: Int,
    val lowStockThreshold: Int = 5,
    val barcode: String? = null
)
