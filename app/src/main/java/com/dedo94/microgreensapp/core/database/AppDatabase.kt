package com.dedo94.microgreensapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dedo94.microgreensapp.core.database.converters.Converters
import com.dedo94.microgreensapp.core.database.dao.EventDao
import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.TrayDao
import com.dedo94.microgreensapp.core.database.dao.TrayStepDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.dao.WeatherDailyDao
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import com.dedo94.microgreensapp.core.database.entity.WeatherDailyEntity

@Database(
    entities = [
        VarietyTemplateEntity::class,
        TemplateStepEntity::class,
        TrayEntity::class,
        TrayStepEntity::class,
        EventEntity::class,
        WeatherDailyEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun varietyTemplateDao(): VarietyTemplateDao
    abstract fun templateStepDao(): TemplateStepDao
    abstract fun trayDao(): TrayDao
    abstract fun trayStepDao(): TrayStepDao
    abstract fun eventDao(): EventDao
    abstract fun weatherDailyDao(): WeatherDailyDao

    companion object {
        const val DATABASE_NAME = "microgreens.db"
    }
}
