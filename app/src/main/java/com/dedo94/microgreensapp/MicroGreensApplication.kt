package com.dedo94.microgreensapp

import android.app.Application
import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.seed.SunflowerTemplateSeed
import com.dedo94.microgreensapp.core.di.ApplicationScope
import com.dedo94.microgreensapp.core.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MicroGreensApplication : Application() {

    @Inject
    lateinit var varietyTemplateDao: VarietyTemplateDao

    @Inject
    lateinit var templateStepDao: TemplateStepDao

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
        // Rieseguito ad ogni avvio (idempotente: countByName la salta se già
        // presente) invece che solo nel Callback.onCreate di Room, così la
        // varietà precaricata torna disponibile anche se il DB viene
        // ricreato da zero per qualunque motivo.
        applicationScope.launch {
            SunflowerTemplateSeed.seedIfNeeded(varietyTemplateDao, templateStepDao)
        }
    }
}
