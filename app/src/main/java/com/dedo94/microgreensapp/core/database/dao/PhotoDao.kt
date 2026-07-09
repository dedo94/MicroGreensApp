package com.dedo94.microgreensapp.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.dedo94.microgreensapp.core.database.entity.PhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: PhotoEntity): Long

    @Delete
    suspend fun delete(photo: PhotoEntity)

    @Query("SELECT * FROM photos WHERE trayId = :trayId ORDER BY createdAt DESC")
    fun getForTray(trayId: Long): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE eventId = :eventId ORDER BY createdAt DESC")
    fun getForEvent(eventId: Long): Flow<List<PhotoEntity>>
}
