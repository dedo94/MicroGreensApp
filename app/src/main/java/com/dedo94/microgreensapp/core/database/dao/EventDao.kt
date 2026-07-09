package com.dedo94.microgreensapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert
    suspend fun insert(event: EventEntity): Long

    @Update
    suspend fun update(event: EventEntity)

    @Delete
    suspend fun delete(event: EventEntity)

    @Query("SELECT * FROM events WHERE trayId = :trayId ORDER BY eventDate, eventTime")
    fun getEventsForTray(trayId: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE eventDate BETWEEN :start AND :end")
    fun getEventsInRange(start: LocalDate, end: LocalDate): Flow<List<EventEntity>>
}
