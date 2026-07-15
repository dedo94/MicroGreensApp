package com.dedo94.microgreensapp.core.repository

import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.TrayDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor(
    private val templateDao: VarietyTemplateDao,
    private val stepDao: TemplateStepDao,
    private val trayDao: TrayDao,
) {
    fun activeTemplates(): Flow<List<VarietyTemplateEntity>> = templateDao.getActive()

    fun observeTemplate(templateId: Long): Flow<VarietyTemplateEntity?> =
        templateDao.observeById(templateId)

    fun stepsForTemplate(templateId: Long): Flow<List<TemplateStepEntity>> =
        stepDao.getStepsForTemplate(templateId)

    suspend fun createTemplate(name: String, plantType: String, notes: String): Long =
        templateDao.insert(
            VarietyTemplateEntity(name = name, plantType = plantType, notes = notes)
        )

    suspend fun updateTemplate(template: VarietyTemplateEntity) =
        templateDao.update(template.copy(updatedAt = Instant.now()))

    /**
     * Elimina definitivamente il template se nessun vassoio lo referenzia;
     * altrimenti lo archivia (soft-delete) così i vassoi esistenti e le
     * statistiche per varietà restano validi, ma il template sparisce dal
     * selettore per i nuovi vassoi.
     */
    suspend fun deleteTemplate(template: VarietyTemplateEntity) {
        if (trayDao.countByTemplate(template.id) > 0) {
            archiveTemplate(template)
        } else {
            templateDao.delete(template)
        }
    }

    suspend fun archiveTemplate(template: VarietyTemplateEntity) =
        templateDao.update(template.copy(isArchived = true, updatedAt = Instant.now()))

    suspend fun addStep(step: TemplateStepEntity): Long = stepDao.insert(step)

    suspend fun updateStep(step: TemplateStepEntity) = stepDao.update(step)

    suspend fun deleteStep(step: TemplateStepEntity) = stepDao.delete(step)

    /** [steps] deve già essere nell'ordine finale desiderato: qui si assegnano solo gli orderIndex. */
    suspend fun reorderSteps(steps: List<TemplateStepEntity>) {
        val reindexed = steps.mapIndexed { index, step -> step.copy(orderIndex = index) }
        stepDao.updateAll(reindexed)
    }

    suspend fun nextOrderIndex(templateId: Long): Int = stepDao.countForTemplate(templateId)
}
