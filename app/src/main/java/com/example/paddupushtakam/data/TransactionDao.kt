package com.example.paddupushtakam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the Transactions table.
 * This interface contains the SQL queries and database operations for transactions.
 * Room automatically generates the implementation for these functions.
 */
@Dao
interface TransactionDao {
    // Inserts a new transaction into the database
    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE deletedAt IS NOT NULL AND deletedAt < :cutoffTime")
    suspend fun deleteOldTransactions(cutoffTime: Long)

    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY timestamp DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    fun getDeletedTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND deletedAt IS NULL")
    fun getTotalAmountByType(type: TransactionType): Flow<Double?>
    
    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND timestamp >= :startTimestamp AND timestamp <= :endTimestamp AND deletedAt IS NULL")
    fun getAmountByTypeForPeriod(type: TransactionType, startTimestamp: Long, endTimestamp: Long): Flow<Double?>

    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>
}
