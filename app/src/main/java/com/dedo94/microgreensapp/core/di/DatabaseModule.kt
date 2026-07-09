package com.dedo94.microgreensapp.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dedo94.microgreensapp.core.database.AppDatabase
import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.seed.SunflowerTemplateSeed
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        varietyTemplateDaoProvider: Provider<VarietyTemplateDao>,
        templateStepDaoProvider: Provider<TemplateStepDao>,
        @ApplicationScope applicationScope: CoroutineScope,
    ): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    applicationScope.launch {
                        SunflowerTemplateSeed.seedIfNeeded(
                            varietyTemplateDaoProvider.get(),
                            templateStepDaoProvider.get(),
                        )
                    }
                }
            })
            .build()

    @Provides
    fun provideVarietyTemplateDao(database: AppDatabase): VarietyTemplateDao =
        database.varietyTemplateDao()

    @Provides
    fun provideTemplateStepDao(database: AppDatabase): TemplateStepDao =
        database.templateStepDao()
}
