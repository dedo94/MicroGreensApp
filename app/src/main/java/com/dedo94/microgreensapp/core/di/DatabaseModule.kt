package com.dedo94.microgreensapp.core.di

import android.content.Context
import androidx.room.Room
import com.dedo94.microgreensapp.core.database.AppDatabase
import com.dedo94.microgreensapp.core.database.MIGRATION_5_6
import com.dedo94.microgreensapp.core.database.MIGRATION_6_7
import com.dedo94.microgreensapp.core.database.dao.EventDao
import com.dedo94.microgreensapp.core.database.dao.TemplatePhaseDao
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
            // Le versioni 1-4 sono schema pre-esistenti per cui non è mai
            // stata scritta una Migration esplicita (venivano ricreate a
            // ogni cambio con fallbackToDestructiveMigration()): senza
            // questa riga, chiunque avesse ancora un DB locale a una di
            // quelle versioni otteneva un crash all'avvio (IllegalStateException:
            // migration mancante) invece del comportamento precedente.
            // Da qui in avanti ogni cambio di schema ha una Migration
            // esplicita (vedi MIGRATION_5_6), per non perdere più i dati
            // dell'utente.
            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4)
            .fallbackToDestructiveMigrationOnDowngrade()
            .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
            .build()

    @Provides
    fun provideVarietyTemplateDao(database: AppDatabase): VarietyTemplateDao =
        database.varietyTemplateDao()

    @Provides
    fun provideTemplatePhaseDao(database: AppDatabase): TemplatePhaseDao =
        database.templatePhaseDao()

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
