package com.dedo94.microgreensapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dedo94.microgreensapp.core.database.entity.WeatherDailyEntity
import java.time.LocalDate

@Dao
interface WeatherDailyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WeatherDailyEntity)

    @Query("SELECT * FROM weather_daily WHERE date = :date")
    suspend fun getForDate(date: LocalDate): WeatherDailyEntity?
}
