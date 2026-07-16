package com.dedo94.microgreensapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateStepDao {

    @Insert
    suspend fun insert(step: TemplateStepEntity): Long

    @Insert
    suspend fun insertAll(steps: List<TemplateStepEntity>): List<Long>

    @Update
    suspend fun update(step: TemplateStepEntity)

    @Update
    suspend fun updateAll(steps: List<TemplateStepEntity>)

    @Delete
    suspend fun delete(step: TemplateStepEntity)

    @Query("SELECT * FROM template_steps WHERE phaseId = :phaseId ORDER BY orderIndex")
    fun getStepsForPhase(phaseId: Long): Flow<List<TemplateStepEntity>>

    @Query("SELECT * FROM template_steps WHERE phaseId = :phaseId ORDER BY orderIndex")
    suspend fun getStepsForPhaseOnce(phaseId: Long): List<TemplateStepEntity>

    @Query("SELECT COUNT(*) FROM template_steps WHERE phaseId = :phaseId")
    suspend fun countForPhase(phaseId: Long): Int
}
