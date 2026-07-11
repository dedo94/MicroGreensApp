package com.dedo94.microgreensapp.core.di

import android.content.Context
import androidx.room.Room
import com.dedo94.microgreensapp.core.database.AppDatabase
import com.dedo94.microgreensapp.core.database.dao.EventDao
import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.TrayDao
import com.dedo94.microgreensapp.core.database.dao.TrayStepDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.dao.WeatherDailyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            // Un downgrade (es. reinstallo di una build più vecchia) non è
            // un caso reale d'uso: va bene ricreare il DB solo in quel caso.
            // Ogni cambio di schema in avanti richiede da qui in poi una
            // Migration esplicita, per non perdere più i dati dell'utente.
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides
    fun provideVarietyTemplateDao(database: AppDatabase): VarietyTemplateDao =
        database.varietyTemplateDao()

    @Provides
    fun provideTemplateStepDao(database: AppDatabase): TemplateStepDao =
        database.templateStepDao()

    @Provides
    fun provideTrayDao(database: AppDatabase): TrayDao = database.trayDao()

    @Provides
    fun provideTrayStepDao(database: AppDatabase): TrayStepDao = database.trayStepDao()

    @Provides
    fun provideEventDao(database: AppDatabase): EventDao = database.eventDao()

    @Provides
    fun provideWeatherDailyDao(database: AppDatabase): WeatherDailyDao = database.weatherDailyDao()

}
