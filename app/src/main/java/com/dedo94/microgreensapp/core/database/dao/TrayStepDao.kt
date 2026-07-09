package com.dedo94.microgreensapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface TrayStepDao {

    @Insert
    suspend fun insert(step: TrayStepEntity): Long

    @Insert
    suspend fun insertAll(steps: List<TrayStepEntity>): List<Long>

    @Update
    suspend fun update(step: TrayStepEntity)

    @Delete
    suspend fun delete(step: TrayStepEntity)

    @Query("SELECT * FROM tray_steps WHERE trayId = :trayId ORDER BY plannedStartDate, orderIndex")
    fun getStepsForTray(trayId: Long): Flow<List<TrayStepEntity>>

    @Query(
        "SELECT * FROM tray_steps WHERE plannedStartDate <= :end AND plannedEndDate >= :start"
    )
    fun getStepsOverlappingRange(start: LocalDate, end: LocalDate): Flow<List<TrayStepEntity>>
}
