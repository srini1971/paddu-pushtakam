package com.example.paddupushtakam.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TransactionEntity::class, CategoryEntity::class, CustomFieldEntity::class, ProductEntity::class], version = 11, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun customFieldDao(): CustomFieldDao
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN deletedAt INTEGER DEFAULT NULL")
            }
        }
        
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN noOfItems INTEGER DEFAULT NULL")
            }
        }
        
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN customFields TEXT DEFAULT NULL")
                database.execSQL("CREATE TABLE IF NOT EXISTS `custom_fields` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL)")
            }
        }
        
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN paymentDetails TEXT DEFAULT NULL")
            }
        }
        
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE transactions ADD COLUMN enteredBy TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table matching the updated TransactionEntity
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transactions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        `description` TEXT, 
                        `category` TEXT NOT NULL, 
                        `paymentMode` TEXT NOT NULL, 
                        `receiptUri` TEXT, 
                        `deletedAt` INTEGER, 
                        `openingStock` INTEGER, 
                        `stockSold` INTEGER, 
                        `stockRemaining` INTEGER, 
                        `customFields` TEXT, 
                        `paymentDetails` TEXT, 
                        `enteredBy` TEXT
                    )
                """.trimIndent())
                
                // Copy the data. We map the old `noOfItems` column to the new `stockSold` column.
                database.execSQL("""
                    INSERT INTO transactions_new (
                        id, amount, type, timestamp, description, category, 
                        paymentMode, receiptUri, deletedAt, stockSold, 
                        customFields, paymentDetails, enteredBy
                    )
                    SELECT 
                        id, amount, type, timestamp, description, category, 
                        paymentMode, receiptUri, deletedAt, noOfItems, 
                        customFields, paymentDetails, enteredBy
                    FROM transactions
                """.trimIndent())
                
                // Remove the old table
                database.execSQL("DROP TABLE transactions")
                
                // Change the table name to the correct one
                database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `products` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `currentStock` INTEGER NOT NULL, `lowStockThreshold` INTEGER NOT NULL)")
                database.execSQL("ALTER TABLE transactions ADD COLUMN productId INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE products ADD COLUMN barcode TEXT DEFAULT NULL")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "paddu_pushtakam_database"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
