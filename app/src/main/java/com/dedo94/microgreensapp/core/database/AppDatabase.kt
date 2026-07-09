package com.dedo94.microgreensapp.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dedo94.microgreensapp.core.database.converters.Converters
import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity

@Database(
    entities = [
        VarietyTemplateEntity::class,
        TemplateStepEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun varietyTemplateDao(): VarietyTemplateDao
    abstract fun templateStepDao(): TemplateStepDao

    companion object {
        const val DATABASE_NAME = "microgreens.db"
    }
}
