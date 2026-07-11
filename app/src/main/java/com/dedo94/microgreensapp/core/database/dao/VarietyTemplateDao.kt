package com.dedo94.microgreensapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VarietyTemplateDao {

    @Insert
    suspend fun insert(template: VarietyTemplateEntity): Long

    @Update
    suspend fun update(template: VarietyTemplateEntity)

    @Delete
    suspend fun delete(template: VarietyTemplateEntity)

    @Query("SELECT * FROM variety_templates WHERE isArchived = 0 ORDER BY name")
    fun getActive(): Flow<List<VarietyTemplateEntity>>

    @Query("SELECT * FROM variety_templates ORDER BY name")
    fun getAll(): Flow<List<VarietyTemplateEntity>>

    @Query("SELECT * FROM variety_templates WHERE id = :id")
    fun observeById(id: Long): Flow<VarietyTemplateEntity?>

    @Query("SELECT * FROM variety_templates WHERE id = :id")
    suspend fun getById(id: Long): VarietyTemplateEntity?

    @Query("SELECT * FROM variety_templates WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): VarietyTemplateEntity?

    @Query("SELECT COUNT(*) FROM variety_templates WHERE name = :name AND id != :excludeId")
    suspend fun countByName(name: String, excludeId: Long = 0L): Int
}
