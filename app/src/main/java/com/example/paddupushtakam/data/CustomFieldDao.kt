package com.example.paddupushtakam.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomFieldDao {
    @Query("SELECT * FROM custom_fields")
    fun getAllCustomFields(): Flow<List<CustomFieldEntity>>

    @Insert
    suspend fun insertCustomField(customField: CustomFieldEntity)
}
