package com.dedo94.microgreensapp.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * weather_daily è solo una cache giornaliera del meteo (vedi KDoc di
 * WeatherDailyEntity): niente da preservare, si ricrea vuota e si
 * ripopola da sola alla prossima chiamata a fetchTodayIfNeeded().
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS weather_daily")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS weather_daily (" +
                "date TEXT NOT NULL PRIMARY KEY, " +
                "fetchedTemperature REAL, " +
                "fetchedHumidity REAL, " +
                "fetchedAt INTEGER NOT NULL, " +
                "locationNameSnapshot TEXT NOT NULL)"
        )
    }
}
