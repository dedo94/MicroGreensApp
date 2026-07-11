package com.dedo94.microgreensapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TrayDao {

    @Insert
    suspend fun insert(tray: TrayEntity): Long

    @Update
    suspend fun update(tray: TrayEntity)

    @Delete
    suspend fun delete(tray: TrayEntity)

    @Query("SELECT * FROM trays ORDER BY sowingDate DESC")
    fun getAll(): Flow<List<TrayEntity>>

    @Query("SELECT * FROM trays WHERE id = :id")
    fun observeById(id: Long): Flow<TrayEntity?>

    @Query("SELECT * FROM trays WHERE status = :status")
    suspend fun getByStatusOnce(status: TrayStatus): List<TrayEntity>

    @Query("SELECT COUNT(*) FROM trays WHERE varietyTemplateId = :templateId")
    suspend fun countByTemplate(templateId: Long): Int
}
