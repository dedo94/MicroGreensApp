package com.dedo94.microgreensapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dedo94.microgreensapp.core.database.entity.TemplatePhaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplatePhaseDao {

    @Insert
    suspend fun insert(phase: TemplatePhaseEntity): Long

    @Update
    suspend fun update(phase: TemplatePhaseEntity)

    @Update
    suspend fun updateAll(phases: List<TemplatePhaseEntity>)

    @Delete
    suspend fun delete(phase: TemplatePhaseEntity)

    @Query("SELECT * FROM template_phases WHERE id = :id")
    fun observeById(id: Long): Flow<TemplatePhaseEntity?>

    @Query("SELECT * FROM template_phases WHERE templateId = :templateId ORDER BY orderIndex")
    fun getPhasesForTemplate(templateId: Long): Flow<List<TemplatePhaseEntity>>

    @Query("SELECT * FROM template_phases WHERE templateId = :templateId ORDER BY orderIndex")
    suspend fun getPhasesForTemplateOnce(templateId: Long): List<TemplatePhaseEntity>

    @Query("SELECT COUNT(*) FROM template_phases WHERE templateId = :templateId")
    suspend fun countForTemplate(templateId: Long): Int
}
